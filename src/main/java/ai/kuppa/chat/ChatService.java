package ai.kuppa.chat;

import ai.kuppa.action.ProposedAction;
import ai.kuppa.action.ProposedActionRepository;
import ai.kuppa.audit.AuditService;
import ai.kuppa.conversation.VayuBrainGateway;
import ai.kuppa.memory.ConversationMemoryCaptureService;
import ai.kuppa.memory.PersonaMemoryRepository;
import ai.kuppa.planner.Plan;
import ai.kuppa.planner.Planner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatService {
    private final ChatMessageRepository chatRepository;
    private final PersonaMemoryRepository memoryRepository;
    private final ProposedActionRepository actionRepository;
    private final Planner planner;
    private final AuditService audit;
    private final ConversationMemoryCaptureService memoryCapture;

    public ChatService(ChatMessageRepository chatRepository, PersonaMemoryRepository memoryRepository,
                       ProposedActionRepository actionRepository, Planner planner, AuditService audit,
                       ConversationMemoryCaptureService memoryCapture) {
        this.chatRepository = chatRepository;
        this.memoryRepository = memoryRepository;
        this.actionRepository = actionRepository;
        this.planner = planner;
        this.audit = audit;
        this.memoryCapture = memoryCapture;
    }

    @Transactional
    public ChatResponse chat(String message) { return chat(message, null, null, null, null); }

    @Transactional
    public ChatResponse chat(String message, String correlationId) { return chat(message, correlationId, null, null, null); }

    @Transactional
    public ChatResponse chat(String message, String correlationId, String turnMode, String parentCorrelationId) {
        return chat(message, correlationId, turnMode, parentCorrelationId, null);
    }

    @Transactional
    public ChatResponse chat(String message, String correlationId, String turnMode,
                             String parentCorrelationId, String clientSessionId) {
        String requestCorrelationId = normalizeCorrelationId(correlationId);
        String requestSessionId = ChatContinuityService.normalizeSessionId(clientSessionId);
        VayuBrainGateway.TurnContext turnContext = VayuBrainGateway.TurnContext.normalize(turnMode, parentCorrelationId);

        chatRepository.save(new ChatMessage(
                "USER", message, requestCorrelationId, turnContext.mode(), turnContext.parentCorrelationId(), requestSessionId));

        ConversationMemoryCaptureService.CaptureOutcome memoryOutcome = memoryCapture.process(message);
        memoryOutcome.memory().ifPresent(memory ->
                audit.record("MEMORY_CAPTURED", memory.getId(),
                        "category=" + memory.getCategory()
                                + ", confidence=" + memory.getConfidence()
                                + ", source=" + memory.getSource()));
        ConversationMemoryCaptureService.MemoryMutation mutation = memoryOutcome.mutation();
        if (mutation.requested()) {
            String detail = "affectedCount=" + mutation.affectedCount()
                    + (mutation.categories().isEmpty() ? "" : ", categories=" + String.join("|", mutation.categories()));
            audit.record("FORGOTTEN".equals(mutation.type()) ? "MEMORY_FORGOTTEN" : "MEMORY_FORGET_NO_MATCH",
                    requestCorrelationId, detail);
        }

        Plan plan = planner.plan(message, memoryRepository.findByActiveTrueOrderByUpdatedAtDesc(),
                requestCorrelationId, turnContext);
        ProposedAction action = null;
        if (plan.hasAction()) {
            action = actionRepository.save(new ProposedAction(plan.actionType(), plan.actionSummary(), plan.actionPayload(), plan.reason(), plan.riskLevel()));
            audit.record("ACTION_PROPOSED", action.getId(), action.getSummary());
        }
        if (plan.brain() != null) {
            VayuBrainGateway.Response brain = plan.brain();
            audit.record("VAYU_HANDOFF", brain.correlationId(),
                    "contract=" + brain.contractVersion() + ", provider=" + brain.provider()
                            + ", degraded=" + brain.degraded() + ", cancelled=" + brain.cancelled()
                            + ", turnMode=" + brain.turnMode()
                            + (brain.parentCorrelationId() == null ? "" : ", parentCorrelationId=" + brain.parentCorrelationId())
                            + ", latencyMs=" + brain.latencyMs()
                            + (brain.errorCode() == null ? "" : ", errorCode=" + brain.errorCode()));
        }

        boolean cancelled = plan.brain() != null && plan.brain().cancelled();
        chatRepository.save(new ChatMessage(
                "KUPPA_AI", plan.response(), requestCorrelationId, turnContext.mode(), turnContext.parentCorrelationId(),
                cancelled ? null : requestSessionId));
        return new ChatResponse(plan.response(), action, plan.brain());
    }

    private String normalizeCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) return UUID.randomUUID().toString();
        return correlationId.trim();
    }

    public record ChatResponse(String message, ProposedAction proposedAction, VayuBrainGateway.Response brain) {}
}
