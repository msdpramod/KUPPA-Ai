package ai.kuppa.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatContinuityServiceTest {
    private ChatMessageRepository repository;
    private ChatContinuityService service;

    @BeforeEach
    void setUp() {
        repository = mock(ChatMessageRepository.class);
        service = new ChatContinuityService(repository);
    }

    @Test
    void restoresOnlyMetadataForLatestCompletedSessionTurn() {
        String session = "f2da1d83-4652-455f-b3bb-c01c1497db18";
        ChatMessage message = new ChatMessage(
                "KUPPA_AI", "private transcript text", "corr-42", "AUTO", null, session);
        when(repository.findFirstByClientSessionIdAndRoleAndCorrelationIdIsNotNullOrderByCreatedAtDesc(session, "KUPPA_AI"))
                .thenReturn(Optional.of(message));

        ChatContinuityService.ResumableTurn result = service.latest(session);

        assertTrue(result.available());
        assertEquals("corr-42", result.correlationId());
        assertNotNull(result.completedAt());
    }

    @Test
    void invalidSessionIdReturnsUnavailableWithoutDatabaseLookup() {
        ChatContinuityService.ResumableTurn result = service.latest("bad session id with spaces");

        assertFalse(result.available());
        assertNull(result.correlationId());
        verifyNoInteractions(repository);
    }

    @Test
    void unknownSessionReturnsUnavailableWithoutFabricatingParent() {
        String session = "7b64e17a-195a-44d1-b134-96a182a1879b";
        when(repository.findFirstByClientSessionIdAndRoleAndCorrelationIdIsNotNullOrderByCreatedAtDesc(session, "KUPPA_AI"))
                .thenReturn(Optional.empty());

        ChatContinuityService.ResumableTurn result = service.latest(session);

        assertFalse(result.available());
        assertNull(result.correlationId());
        assertNull(result.completedAt());
    }
}
