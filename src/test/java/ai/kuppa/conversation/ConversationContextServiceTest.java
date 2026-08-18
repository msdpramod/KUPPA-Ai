package ai.kuppa.conversation;

import ai.kuppa.chat.ChatMessage;
import ai.kuppa.chat.ChatMessageRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversationContextServiceTest {

    @Test
    void keepsRecentTurnsChronologicalAndDoesNotDuplicateCurrentMessage() {
        ChatMessageRepository repository = mock(ChatMessageRepository.class);
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(
                new ChatMessage("USER", "what about the second one?"),
                new ChatMessage("KUPPA_AI", "Here are two options."),
                new ChatMessage("USER", "give me two options")
        ));

        ConversationContextService service = new ConversationContextService(repository);
        List<ConversationContextService.ConversationTurn> turns = service.recentTurns("what about the second one?");

        assertEquals(3, turns.size());
        assertEquals("give me two options", turns.get(0).content());
        assertEquals("assistant", turns.get(1).role());
        assertEquals("what about the second one?", turns.get(2).content());
    }

    @Test
    void appendsCurrentMessageWhenItHasNotBeenPersistedYet() {
        ChatMessageRepository repository = mock(ChatMessageRepository.class);
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(
                new ChatMessage("KUPPA_AI", "Previous answer"),
                new ChatMessage("USER", "Previous question")
        ));

        ConversationContextService service = new ConversationContextService(repository);
        List<ConversationContextService.ConversationTurn> turns = service.recentTurns("new follow-up");

        assertEquals(3, turns.size());
        assertEquals("new follow-up", turns.get(2).content());
        assertEquals("user", turns.get(2).role());
    }

    @Test
    void limitsContextToTwelveMostRecentStoredTurns() {
        ChatMessageRepository repository = mock(ChatMessageRepository.class);
        List<ChatMessage> newestFirst = new ArrayList<>();
        for (int i = 20; i >= 1; i--) {
            newestFirst.add(new ChatMessage("USER", "message-" + i));
        }
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(newestFirst);

        ConversationContextService service = new ConversationContextService(repository);
        List<ConversationContextService.ConversationTurn> turns = service.recentTurns("message-20");

        assertEquals(12, turns.size());
        assertEquals("message-9", turns.get(0).content());
        assertEquals("message-20", turns.get(11).content());
    }
}
