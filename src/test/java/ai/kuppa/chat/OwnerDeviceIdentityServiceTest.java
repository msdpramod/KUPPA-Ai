package ai.kuppa.chat;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class OwnerDeviceIdentityServiceTest {
    private static final String STRONG_SECRET = "owner-enrollment-secret-0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2026-08-28T03:00:00Z");

    @Test
    void ownerEnrollmentIssuesSignedDeviceCredential() {
        OwnerDeviceIdentityService service = serviceAt(NOW, 3600);

        OwnerDeviceIdentityService.DeviceCredential credential = service.enroll(STRONG_SECRET, "Pramod MacBook");

        assertEquals("owner", credential.ownerId());
        assertEquals("Pramod MacBook", credential.deviceLabel());
        assertNotNull(credential.deviceId());
        assertEquals(NOW.plusSeconds(3600), credential.expiresAt());
        assertTrue(service.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void rejectsWrongOwnerEnrollmentSecret() {
        OwnerDeviceIdentityService service = serviceAt(NOW, 3600);

        assertThrows(SecurityException.class, () -> service.enroll("wrong-secret", "browser"));
    }

    @Test
    void rejectsTamperedDeviceIdentityOrToken() {
        OwnerDeviceIdentityService service = serviceAt(NOW, 3600);
        OwnerDeviceIdentityService.DeviceCredential credential = service.enroll(STRONG_SECRET, "browser");

        assertFalse(service.validate("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", credential.token()));
        assertFalse(service.validate(credential.deviceId(), credential.token() + "tampered"));
    }

    @Test
    void rejectsExpiredDeviceCredential() {
        OwnerDeviceIdentityService issuer = serviceAt(NOW, 60);
        OwnerDeviceIdentityService.DeviceCredential credential = issuer.enroll(STRONG_SECRET, "browser");
        OwnerDeviceIdentityService later = serviceAt(NOW.plusSeconds(61), 60);

        assertFalse(later.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void failsClosedWhenEnrollmentSecretIsWeak() {
        OwnerDeviceIdentityService service = new OwnerDeviceIdentityService(
                "owner",
                "short-secret",
                3600,
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertFalse(service.enabled());
        assertFalse(service.validate("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "v1.1.invalid"));
        assertThrows(IllegalStateException.class, () -> service.enroll("short-secret", "browser"));
    }

    @Test
    void normalizesLongOrBlankDeviceLabelsWithoutUsingThemForAuthorization() {
        OwnerDeviceIdentityService service = serviceAt(NOW, 3600);

        OwnerDeviceIdentityService.DeviceCredential blank = service.enroll(STRONG_SECRET, "   ");
        OwnerDeviceIdentityService.DeviceCredential longLabel = service.enroll(STRONG_SECRET, "x".repeat(120));

        assertEquals("device", blank.deviceLabel());
        assertEquals(80, longLabel.deviceLabel().length());
        assertTrue(service.validate(longLabel.deviceId(), longLabel.token()));
    }

    private OwnerDeviceIdentityService serviceAt(Instant instant, long ttlSeconds) {
        return new OwnerDeviceIdentityService(
                "owner",
                STRONG_SECRET,
                ttlSeconds,
                Clock.fixed(instant, ZoneOffset.UTC));
    }
}
