package ai.kuppa.conversation;

import ai.kuppa.chat.ChatMessage;
import ai.kuppa.chat.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ConversationContextService {
    private static final int MAX_RECENT_TURNS = 12;

    private final ChatMessageRepository chatRepository;

    public ConversationContextService(ChatMessageRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<ConversationTurn> recentTurns(String currentMessage) {
        return recentTurns(currentMessage, VayuBrainGateway.TurnContext.auto());
    }

    public List<ConversationTurn> recentTurns(String currentMessage, VayuBrainGateway.TurnContext turnContext) {
        List<ChatMessage> recent = new ArrayList<>(chatRepository.findTop50ByOrderByCreatedAtDesc());
        if (recent.size() > MAX_RECENT_TURNS) {
            recent = new ArrayList<>(recent.subList(0, MAX_RECENT_TURNS));
        }
        Collections.reverse(recent);

        List<ConversationTurn> turns = map(recent);
        VayuBrainGateway.TurnContext normalized = turnContext == null
                ? VayuBrainGateway.TurnContext.auto()
                : turnContext.normalized();

        if (requiresParent(normalized) && normalized.parentCorrelationId() != null) {
            List<ConversationTurn> parentTurns = map(
                    chatRepository.findByCorrelationIdOrderByCreatedAtAsc(normalized.parentCorrelationId()));
            prependMissingParentTurns(turns, parentTurns);
        }

        String cleanCurrent = currentMessage == null ? "" : currentMessage.trim();
        if (!cleanCurrent.isBlank() && !alreadyContainsCurrentTurn(turns, cleanCurrent)) {
            turns.add(new ConversationTurn("user", cleanCurrent));
        }
        return List.copyOf(turns);
    }

    private boolean requiresParent(VayuBrainGateway.TurnContext context) {
        return "CONTINUE".equals(context.mode()) || "CORRECTION".equals(context.mode());
    }

    private List<ConversationTurn> map(List<ChatMessage> messages) {
        List<ConversationTurn> turns = new ArrayList<>();
        for (ChatMessage item : messages) {
            String role = "USER".equals(item.getRole()) ? "user" : "assistant";
            turns.add(new ConversationTurn(role, item.getContent()));
        }
        return turns;
    }

    private void prependMissingParentTurns(List<ConversationTurn> turns, List<ConversationTurn> parentTurns) {
        if (parentTurns.isEmpty()) return;
        List<ConversationTurn> missing = new ArrayList<>();
        for (ConversationTurn parent : parentTurns) {
            if (!turns.contains(parent)) missing.add(parent);
        }
        if (!missing.isEmpty()) turns.addAll(0, missing);
    }

    private boolean alreadyContainsCurrentTurn(List<ConversationTurn> turns, String currentMessage) {
        if (turns.isEmpty()) return false;
        ConversationTurn latest = turns.get(turns.size() - 1);
        return "user".equals(latest.role()) && currentMessage.equals(latest.content().trim());
    }

    public record ConversationTurn(String role, String content) {}
}
