package ai.kuppa.audit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OwnerMemoryHistoryServiceTest {
    @Test
    void returnsOnlyTypedPrivacySafeMemoryMetadata() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        AuditEvent captured = new AuditEvent("MEMORY_CAPTURED", "private-memory-id",
                "category=PREFERENCE, confidence=1.0, source=OWNER_EXPLICIT");
        AuditEvent forgotten = new AuditEvent("MEMORY_FORGOTTEN", "correlation-id",
                "affectedCount=2, categories=PREFERENCE|ROUTINE");
        when(repository.findByEventTypeInOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(captured, forgotten));

        var history = new OwnerMemoryHistoryService(repository).history(50);

        assertEquals(2, history.size());
        var first = history.get(0);
        assertEquals("MEMORY_CAPTURED", first.eventType());
        assertEquals("PREFERENCE", first.category());
        assertEquals(1.0, first.confidence());
        assertEquals("OWNER_EXPLICIT", first.source());
        assertNull(first.affectedCount());
        assertNotNull(first.createdAt());

        var second = history.get(1);
        assertEquals("MEMORY_FORGOTTEN", second.eventType());
        assertEquals(2, second.affectedCount());
        assertEquals(List.of("PREFERENCE", "ROUTINE"), second.categories());
        assertNull(second.category());

        assertFalse(first.toString().contains("private-memory-id"));
        assertFalse(second.toString().contains("correlation-id"));
        verify(repository).findByEventTypeInOrderByCreatedAtDesc(any());
        verify(repository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void clampsHistoryLimitAndToleratesMalformedAuditMetadata() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        AuditEvent malformed = new AuditEvent("MEMORY_FORGET_NO_MATCH", "correlation-id",
                "affectedCount=-5, confidence=9.4, categories=PREFERENCE||PREFERENCE|A_VERY_LONG_CATEGORY_NAME_THAT_STILL_STAYS_BOUNDED");
        List<AuditEvent> events = java.util.stream.IntStream.range(0, 120)
                .mapToObj(i -> malformed)
                .toList();
        when(repository.findByEventTypeInOrderByCreatedAtDesc(any())).thenReturn(events);

        var history = new OwnerMemoryHistoryService(repository).history(500);

        assertEquals(100, history.size());
        assertNull(history.get(0).affectedCount());
        assertNull(history.get(0).confidence());
        assertTrue(history.get(0).categories().contains("PREFERENCE"));
    }
}
