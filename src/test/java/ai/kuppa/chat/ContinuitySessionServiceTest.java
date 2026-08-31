package ai.kuppa.chat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ContinuitySessionServiceTest {
    private static final String STRONG_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Instant NOW = Instant.parse("2026-08-27T10:30:00Z");

    @Test
    void springConfigurationConstructorIsExplicitlyAutowired() throws NoSuchMethodException {
        Constructor<ContinuitySessionService> constructor = ContinuitySessionService.class
                .getConstructor(String.class, long.class);

        assertTrue(constructor.isAnnotationPresent(Autowired.class),
                "Spring must explicitly select the configuration constructor because this service has multiple constructors");
    }

    @Test
    void issuesAndValidatesServerSignedSessionCredential() {
        ContinuitySessionService service = serviceAt(NOW, 3600);

        ContinuitySessionService.SessionCredential credential = service.issue();

        assertNotNull(credential.clientSessionId());
        assertNotNull(credential.token());
        assertEquals(NOW.plusSeconds(3600), credential.expiresAt());
        assertTrue(service.validate(credential.clientSessionId(), credential.token()));
    }

    @Test
    void rejectsTamperedSessionOrSignature() {
        ContinuitySessionService service = serviceAt(NOW, 3600);
        ContinuitySessionService.SessionCredential credential = service.issue();

        assertFalse(service.validate("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", credential.token()));
        assertFalse(service.validate(credential.clientSessionId(), credential.token() + "tampered"));
    }

    @Test
    void rejectsExpiredCredential() {
        ContinuitySessionService issuer = serviceAt(NOW, 60);
        ContinuitySessionService.SessionCredential credential = issuer.issue();
        ContinuitySessionService later = serviceAt(NOW.plusSeconds(61), 60);

        assertFalse(later.validate(credential.clientSessionId(), credential.token()));
    }

    @Test
    void failsClosedWhenSigningSecretIsNotConfiguredStrongly() {
        ContinuitySessionService service = new ContinuitySessionService("short-secret", 3600,
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertFalse(service.enabled());
        assertFalse(service.validate("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "1.invalid"));
        assertThrows(IllegalStateException.class, service::issue);
    }

    private ContinuitySessionService serviceAt(Instant instant, long ttlSeconds) {
        return new ContinuitySessionService(
                STRONG_SECRET,
                ttlSeconds,
                Clock.fixed(instant, ZoneOffset.UTC));
    }
}
