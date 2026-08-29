package ai.kuppa.chat;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OwnerDeviceTrustServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-29T11:00:00Z");

    @Test
    void explicitEnrollmentRegistersActiveDevice() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        when(repository.save(any(OwnerDeviceTrust.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        OwnerDeviceIdentityService.DeviceCredential credential =
                new OwnerDeviceIdentityService.DeviceCredential(
                        "owner", "device-1", "Laptop", "v2", "v2.999.signature", NOW.plusSeconds(3600));

        OwnerDeviceTrust trust = service.register(credential);

        assertEquals("device-1", trust.getDeviceId());
        assertEquals("owner", trust.getOwnerId());
        assertNull(trust.getRevokedAt());
        verify(repository).save(any(OwnerDeviceTrust.class));
    }

    @Test
    void validatedLegacyDeviceMigratesOnceThenCanBeRevokedPermanently() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        when(repository.findById("legacy-device"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(activeTrust("legacy-device")))
                .thenReturn(Optional.of(revokedTrust("legacy-device")));
        when(repository.save(any(OwnerDeviceTrust.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertTrue(service.authorizeValidatedCredential("owner", "legacy-device", "v1.999.signature"));
        assertTrue(service.revoke("owner", "legacy-device"));
        assertFalse(service.authorizeValidatedCredential("owner", "legacy-device", "v1.999.signature"));
    }

    @Test
    void continuityIssuanceIsAudited() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        OwnerDeviceTrust trust = activeTrust("device-2");
        when(repository.findById("device-2")).thenReturn(Optional.of(trust));
        when(repository.save(any(OwnerDeviceTrust.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertTrue(service.recordContinuityIssue("owner", "device-2"));
        assertEquals(1L, trust.getContinuityIssueCount());
        assertEquals(NOW, trust.getLastContinuityIssuedAt());
    }

    @Test
    void ownerMismatchFailsClosed() {
        OwnerDeviceTrustRepository repository = mock(OwnerDeviceTrustRepository.class);
        when(repository.findById("device-3")).thenReturn(Optional.of(activeTrust("device-3")));
        OwnerDeviceTrustService service = new OwnerDeviceTrustService(
                repository, Clock.fixed(NOW, ZoneOffset.UTC));

        assertFalse(service.authorizeValidatedCredential("different-owner", "device-3", "v2.999.signature"));
        assertFalse(service.recordContinuityIssue("different-owner", "device-3"));
        assertFalse(service.revoke("different-owner", "device-3"));
    }

    private OwnerDeviceTrust activeTrust(String deviceId) {
        return new OwnerDeviceTrust("owner", deviceId, "Laptop", "v2", NOW.minusSeconds(120));
    }

    private OwnerDeviceTrust revokedTrust(String deviceId) {
        OwnerDeviceTrust trust = activeTrust(deviceId);
        trust.revoke(NOW.minusSeconds(1));
        return trust;
    }
}
