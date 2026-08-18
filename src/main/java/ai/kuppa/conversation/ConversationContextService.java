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
        List<ChatMessage> recent = new ArrayList<>(chatRepository.findTop50ByOrderByCreatedAtDesc());
        if (recent.size() > MAX_RECENT_TURNS) {
            recent = new ArrayList<>(recent.subList(0, MAX_RECENT_TURNS));
        }
        Collections.reverse(recent);

        List<ConversationTurn> turns = new ArrayList<>();
        for (ChatMessage item : recent) {
            String role = "USER".equals(item.getRole()) ? "user" : "assistant";
            turns.add(new ConversationTurn(role, item.getContent()));
        }

        String cleanCurrent = currentMessage == null ? "" : currentMessage.trim();
        if (!cleanCurrent.isBlank() && !alreadyContainsCurrentTurn(turns, cleanCurrent)) {
            turns.add(new ConversationTurn("user", cleanCurrent));
        }
        return List.copyOf(turns);
    }

    private boolean alreadyContainsCurrentTurn(List<ConversationTurn> turns, String currentMessage) {
        if (turns.isEmpty()) return false;
        ConversationTurn latest = turns.get(turns.size() - 1);
        return "user".equals(latest.role()) && currentMessage.equals(latest.content().trim());
    }

    public record ConversationTurn(String role, String content) {}
}
