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
    public ChatResponse chat(String message) {
        chatRepository.save(new ChatMessage("USER", message));

        memoryCapture.capture(message).ifPresent(memory ->
                audit.record("MEMORY_CAPTURED", memory.getId(),
                        memory.getCategory() + ": " + memory.getContent()));

        Plan plan = planner.plan(message, memoryRepository.findByActiveTrueOrderByUpdatedAtDesc());
        ProposedAction action = null;
        if (plan.hasAction()) {
            action = actionRepository.save(new ProposedAction(plan.actionType(), plan.actionSummary(), plan.actionPayload(), plan.reason(), plan.riskLevel()));
            audit.record("ACTION_PROPOSED", action.getId(), action.getSummary());
        }
        if (plan.brain() != null) {
            VayuBrainGateway.Response brain = plan.brain();
            audit.record("VAYU_HANDOFF", brain.correlationId(),
                    "contract=" + brain.contractVersion()
                            + ", provider=" + brain.provider()
                            + ", degraded=" + brain.degraded()
                            + ", latencyMs=" + brain.latencyMs()
                            + (brain.errorCode() == null ? "" : ", errorCode=" + brain.errorCode()));
        }
        chatRepository.save(new ChatMessage("KUPPA_AI", plan.response()));
        return new ChatResponse(plan.response(), action, plan.brain());
    }

    public record ChatResponse(String message, ProposedAction proposedAction, VayuBrainGateway.Response brain) {}
}
