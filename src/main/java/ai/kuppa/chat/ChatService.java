package ai.kuppa.chat;

import ai.kuppa.action.ProposedAction;
import ai.kuppa.action.ProposedActionRepository;
import ai.kuppa.adaptive.AdaptivePersonaService;
import ai.kuppa.audit.AuditService;
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
    private final AdaptivePersonaService adaptivePersonaService;

    public ChatService(ChatMessageRepository chatRepository, PersonaMemoryRepository memoryRepository,
                       ProposedActionRepository actionRepository, Planner planner, AuditService audit,
                       AdaptivePersonaService adaptivePersonaService) {
        this.chatRepository = chatRepository;
        this.memoryRepository = memoryRepository;
        this.actionRepository = actionRepository;
        this.planner = planner;
        this.audit = audit;
        this.adaptivePersonaService = adaptivePersonaService;
    }

    @Transactional
    public ChatResponse chat(String message) {
        chatRepository.save(new ChatMessage("USER", message));
        adaptivePersonaService.observeUserMessage(message);

        Plan plan = planner.plan(message, memoryRepository.findByActiveTrueOrderByCreatedAtDesc());
        ProposedAction action = null;
        if (plan.hasAction()) {
            action = actionRepository.save(new ProposedAction(plan.actionType(), plan.actionSummary(), plan.actionPayload(), plan.reason(), plan.riskLevel()));
            audit.record("ACTION_PROPOSED", action.getId(), action.getSummary());
        }
        chatRepository.save(new ChatMessage("KUPPA_AI", plan.response()));
        return new ChatResponse(plan.response(), action);
    }

    public record ChatResponse(String message, ProposedAction proposedAction) {}
}
