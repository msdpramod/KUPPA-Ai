package ai.kuppa.audit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OwnerTrustHistoryServiceTest {
    @Test
    void returnsOnlyTypedTrustMetadataAndHonorsDeviceFilter() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        AuditEvent event = new AuditEvent("OWNER_DEVICE_REVOKED_REMOTE", "device-1",
                "actor=OWNER_MANAGEMENT;reason=REMOTE_REVOCATION");
        when(repository.findByEventTypeInAndActionIdOrderByCreatedAtDesc(any(), eq("device-1")))
                .thenReturn(List.of(event));

        OwnerTrustHistoryService service = new OwnerTrustHistoryService(repository);
        var history = service.history(" device-1 ", 50);

        assertEquals(1, history.size());
        var item = history.get(0);
        assertEquals("OWNER_DEVICE_REVOKED_REMOTE", item.eventType());
        assertEquals("device-1", item.deviceId());
        assertEquals("OWNER_MANAGEMENT", item.actor());
        assertEquals("REMOTE_REVOCATION", item.reason());
        assertNotNull(item.createdAt());
        verify(repository).findByEventTypeInAndActionIdOrderByCreatedAtDesc(any(), eq("device-1"));
        verify(repository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void clampsHistoryLimitToOneHundred() {
        AuditEventRepository repository = mock(AuditEventRepository.class);
        List<AuditEvent> events = java.util.stream.IntStream.range(0, 120)
                .mapToObj(i -> new AuditEvent("OWNER_DEVICE_CONTINUITY_ISSUED", "device-" + i,
                        "actor=DEVICE;reason=CONTINUITY_ISSUED"))
                .toList();
        when(repository.findByEventTypeInOrderByCreatedAtDesc(any())).thenReturn(events);

        var history = new OwnerTrustHistoryService(repository).history(null, 500);
        assertEquals(100, history.size());
    }
}
