package ai.kuppa.chat;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class OwnerDeviceIdentityServiceTest {
    private static final String ENROLLMENT_SECRET = "owner-enrollment-secret-0123456789abcdef0123456789abcdef";
    private static final String SIGNING_SECRET = "device-signing-secret-0123456789abcdef0123456789abcdef";
    private static final String NEXT_SIGNING_SECRET = "device-signing-secret-next-0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2026-08-29T03:00:00Z");

    @Test
    void dedicatedSigningIssuesV2Credential() {
        OwnerDeviceIdentityService service = serviceAt(NOW, SIGNING_SECRET, "", 3600);

        OwnerDeviceIdentityService.DeviceCredential credential = service.enroll(ENROLLMENT_SECRET, "Pramod MacBook");

        assertEquals("owner", credential.ownerId());
        assertEquals("Pramod MacBook", credential.deviceLabel());
        assertEquals("v2", credential.tokenVersion());
        assertTrue(credential.token().startsWith("v2."));
        assertEquals("DEDICATED_V2", service.signingMode());
        assertEquals(NOW.plusSeconds(3600), credential.expiresAt());
        assertTrue(service.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void legacyModeRemainsBackwardCompatibleWhenDedicatedSecretIsNotConfigured() {
        OwnerDeviceIdentityService service = serviceAt(NOW, "", "", 3600);

        OwnerDeviceIdentityService.DeviceCredential credential = service.enroll(ENROLLMENT_SECRET, "browser");

        assertEquals("v1", credential.tokenVersion());
        assertEquals("LEGACY_V1", service.signingMode());
        assertTrue(service.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void acceptsPreviousSigningSecretDuringRotationWindow() {
        OwnerDeviceIdentityService issuer = serviceAt(NOW, SIGNING_SECRET, "", 3600);
        OwnerDeviceIdentityService.DeviceCredential credential = issuer.enroll(ENROLLMENT_SECRET, "browser");
        OwnerDeviceIdentityService rotated = serviceAt(NOW.plusSeconds(30), NEXT_SIGNING_SECRET, SIGNING_SECRET, 3600);

        assertTrue(rotated.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void rejectsOldSigningSecretWhenItIsNotConfiguredAsPrevious() {
        OwnerDeviceIdentityService issuer = serviceAt(NOW, SIGNING_SECRET, "", 3600);
        OwnerDeviceIdentityService.DeviceCredential credential = issuer.enroll(ENROLLMENT_SECRET, "browser");
        OwnerDeviceIdentityService rotated = serviceAt(NOW.plusSeconds(30), NEXT_SIGNING_SECRET, "", 3600);

        assertFalse(rotated.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void legacyV1CredentialSurvivesMigrationToDedicatedSigningUntilExpiry() {
        OwnerDeviceIdentityService legacyIssuer = serviceAt(NOW, "", "", 3600);
        OwnerDeviceIdentityService.DeviceCredential credential = legacyIssuer.enroll(ENROLLMENT_SECRET, "browser");
        OwnerDeviceIdentityService migrated = serviceAt(NOW.plusSeconds(30), SIGNING_SECRET, "", 3600);

        assertEquals("v1", credential.tokenVersion());
        assertTrue(migrated.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void rejectsWrongOwnerEnrollmentSecret() {
        OwnerDeviceIdentityService service = serviceAt(NOW, SIGNING_SECRET, "", 3600);
        assertThrows(SecurityException.class, () -> service.enroll("wrong-secret", "browser"));
    }

    @Test
    void rejectsTamperedDeviceIdentityOrToken() {
        OwnerDeviceIdentityService service = serviceAt(NOW, SIGNING_SECRET, "", 3600);
        OwnerDeviceIdentityService.DeviceCredential credential = service.enroll(ENROLLMENT_SECRET, "browser");

        assertFalse(service.validate("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", credential.token()));
        assertFalse(service.validate(credential.deviceId(), credential.token() + "tampered"));
    }

    @Test
    void rejectsExpiredDeviceCredential() {
        OwnerDeviceIdentityService issuer = serviceAt(NOW, SIGNING_SECRET, "", 60);
        OwnerDeviceIdentityService.DeviceCredential credential = issuer.enroll(ENROLLMENT_SECRET, "browser");
        OwnerDeviceIdentityService later = serviceAt(NOW.plusSeconds(61), SIGNING_SECRET, "", 60);

        assertFalse(later.validate(credential.deviceId(), credential.token()));
    }

    @Test
    void failsClosedForWeakDedicatedSigningConfiguration() {
        OwnerDeviceIdentityService service = serviceAt(NOW, "short", "", 3600);

        assertFalse(service.enabled());
        assertThrows(IllegalStateException.class, () -> service.enroll(ENROLLMENT_SECRET, "browser"));
    }

    @Test
    void failsClosedForPreviousSecretWithoutStrongActiveSigningSecret() {
        OwnerDeviceIdentityService service = serviceAt(NOW, "", SIGNING_SECRET, 3600);
        assertFalse(service.enabled());
    }

    @Test
    void normalizesLongOrBlankDeviceLabelsWithoutUsingThemForAuthorization() {
        OwnerDeviceIdentityService service = serviceAt(NOW, SIGNING_SECRET, "", 3600);

        OwnerDeviceIdentityService.DeviceCredential blank = service.enroll(ENROLLMENT_SECRET, "   ");
        OwnerDeviceIdentityService.DeviceCredential longLabel = service.enroll(ENROLLMENT_SECRET, "x".repeat(120));

        assertEquals("device", blank.deviceLabel());
        assertEquals(80, longLabel.deviceLabel().length());
        assertTrue(service.validate(longLabel.deviceId(), longLabel.token()));
    }

    private OwnerDeviceIdentityService serviceAt(Instant instant, String signingSecret,
                                                  String previousSigningSecret, long ttlSeconds) {
        return new OwnerDeviceIdentityService(
                "owner",
                ENROLLMENT_SECRET,
                signingSecret,
                previousSigningSecret,
                ttlSeconds,
                Clock.fixed(instant, ZoneOffset.UTC));
    }
}
