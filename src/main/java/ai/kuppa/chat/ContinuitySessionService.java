package ai.kuppa.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class ContinuitySessionService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_LENGTH = 32;

    private final byte[] secret;
    private final long ttlSeconds;
    private final Clock clock;

    @Autowired
    public ContinuitySessionService(
            @Value("${kuppa.continuity.signing-secret:}") String signingSecret,
            @Value("${kuppa.continuity.session-ttl-seconds:2592000}") long ttlSeconds) {
        this(signingSecret, ttlSeconds, Clock.systemUTC());
    }

    ContinuitySessionService(String signingSecret, long ttlSeconds, Clock clock) {
        this.secret = signingSecret == null ? new byte[0] : signingSecret.getBytes(StandardCharsets.UTF_8);
        if (ttlSeconds <= 0) throw new IllegalArgumentException("Continuity session TTL must be positive");
        this.ttlSeconds = ttlSeconds;
        this.clock = clock;
    }

    public boolean enabled() {
        return secret.length >= MIN_SECRET_LENGTH;
    }

    public SessionCredential issue() {
        if (!enabled()) {
            throw new IllegalStateException("Secure continuity sessions are not configured");
        }
        String clientSessionId = UUID.randomUUID().toString();
        Instant expiresAt = clock.instant().plusSeconds(ttlSeconds);
        String token = tokenFor(clientSessionId, expiresAt.getEpochSecond());
        return new SessionCredential(clientSessionId, token, expiresAt);
    }

    public boolean validate(String clientSessionId, String token) {
        if (!enabled() || token == null) return false;
        String session = ChatContinuityService.normalizeSessionId(clientSessionId);
        if (session == null) return false;

        String[] parts = token.split("\\.", -1);
        if (parts.length != 2) return false;
        try {
            long expiresAtEpochSecond = Long.parseLong(parts[0]);
            if (expiresAtEpochSecond <= clock.instant().getEpochSecond()) return false;
            byte[] supplied = Base64.getUrlDecoder().decode(parts[1]);
            byte[] expected = hmac(session + "." + expiresAtEpochSecond);
            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String tokenFor(String clientSessionId, long expiresAtEpochSecond) {
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmac(clientSessionId + "." + expiresAtEpochSecond));
        return expiresAtEpochSecond + "." + signature;
    }

    private byte[] hmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign continuity session", ex);
        }
    }

    public record SessionCredential(String clientSessionId, String token, Instant expiresAt) {}
}
