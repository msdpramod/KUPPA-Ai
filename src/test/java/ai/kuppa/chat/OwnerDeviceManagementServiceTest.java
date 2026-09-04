package ai.kuppa.chat;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OwnerDeviceManagementServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-30T00:00:00Z");

    @Test
    void inventoryReturnsOnlyRepositoryScopedOwnerMetadata() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        OwnerDeviceTrust trust = new OwnerDeviceTrust("owner", "device-1", "Laptop", "v2", NOW.minusSeconds(60));
        when(repository.findByOwnerIdOrderByEnrolledAtDesc("owner")).thenReturn(List.of(trust));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        List<OwnerDeviceTrustService.DeviceSummary> devices = service.inventory("owner");

        assertEquals(1, devices.size());
        assertEquals("device-1", devices.get(0).deviceId());
        assertEquals("Laptop", devices.get(0).deviceLabel());
        assertTrue(devices.get(0).active());
    }

    @Test
    void remoteRevocationIsOwnerScopedAndIdempotent() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        OwnerDeviceTrust trust = new OwnerDeviceTrust("owner", "lost-device", "Phone", "v2", NOW.minusSeconds(60));
        when(repository.findById("lost-device")).thenReturn(Optional.of(trust));
        when(repository.save(any(OwnerDeviceTrust.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        OwnerDeviceTrustService.DeviceSummary first = service.remoteRevoke("owner", "lost-device");
        OwnerDeviceTrustService.DeviceSummary second = service.remoteRevoke("owner", "lost-device");

        assertNotNull(first);
        assertFalse(first.active());
        assertEquals(NOW, first.revokedAt());
        assertNotNull(second);
        assertEquals(NOW, second.revokedAt());
        verify(repository, times(2)).save(trust);
    }

    @Test
    void remoteRevocationCannotCrossOwnerBoundary() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        OwnerDeviceTrust trust = new OwnerDeviceTrust("owner", "device-2", "Tablet", "v2", NOW.minusSeconds(60));
        when(repository.findById("device-2")).thenReturn(Optional.of(trust));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertNull(service.remoteRevoke("different-owner", "device-2"));
        verify(repository, never()).save(any());
    }
}
