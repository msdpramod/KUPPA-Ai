package ai.kuppa.chat;

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
public class OwnerDeviceIdentityService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_LENGTH = 32;
    private static final int MAX_DEVICE_LABEL_LENGTH = 80;
    private static final String TOKEN_VERSION = "v1";

    private final String ownerId;
    private final byte[] enrollmentSecret;
    private final long tokenTtlSeconds;
    private final Clock clock;

    public OwnerDeviceIdentityService(
            @Value("${kuppa.identity.owner-id:owner}") String ownerId,
            @Value("${kuppa.identity.enrollment-secret:}") String enrollmentSecret,
            @Value("${kuppa.identity.device-token-ttl-seconds:7776000}") long tokenTtlSeconds) {
        this(ownerId, enrollmentSecret, tokenTtlSeconds, Clock.systemUTC());
    }

    OwnerDeviceIdentityService(String ownerId, String enrollmentSecret, long tokenTtlSeconds, Clock clock) {
        String normalizedOwnerId = ownerId == null ? "" : ownerId.trim();
        if (normalizedOwnerId.isEmpty()) throw new IllegalArgumentException("Owner id must not be blank");
        if (tokenTtlSeconds <= 0) throw new IllegalArgumentException("Device token TTL must be positive");
        this.ownerId = normalizedOwnerId;
        this.enrollmentSecret = enrollmentSecret == null
                ? new byte[0]
                : enrollmentSecret.getBytes(StandardCharsets.UTF_8);
        this.tokenTtlSeconds = tokenTtlSeconds;
        this.clock = clock;
    }

    public boolean enabled() {
        return enrollmentSecret.length >= MIN_SECRET_LENGTH;
    }

    public String ownerId() {
        return ownerId;
    }

    public DeviceCredential enroll(String presentedEnrollmentSecret, String requestedDeviceLabel) {
        if (!enabled()) {
            throw new IllegalStateException("Owner device enrollment is not configured");
        }
        if (!matchesEnrollmentSecret(presentedEnrollmentSecret)) {
            throw new SecurityException("Owner enrollment credential rejected");
        }

        String deviceId = UUID.randomUUID().toString();
        String deviceLabel = normalizeDeviceLabel(requestedDeviceLabel);
        Instant expiresAt = clock.instant().plusSeconds(tokenTtlSeconds);
        String token = tokenFor(deviceId, expiresAt.getEpochSecond());
        return new DeviceCredential(ownerId, deviceId, deviceLabel, token, expiresAt);
    }

    public boolean validate(String deviceId, String token) {
        if (!enabled() || deviceId == null || deviceId.isBlank() || token == null) return false;

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || !TOKEN_VERSION.equals(parts[0])) return false;
        try {
            long expiresAtEpochSecond = Long.parseLong(parts[1]);
            if (expiresAtEpochSecond <= clock.instant().getEpochSecond()) return false;
            byte[] supplied = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = hmac(signingPayload(deviceId, expiresAtEpochSecond));
            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean matchesEnrollmentSecret(String presentedEnrollmentSecret) {
        if (presentedEnrollmentSecret == null) return false;
        return MessageDigest.isEqual(
                enrollmentSecret,
                presentedEnrollmentSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String tokenFor(String deviceId, long expiresAtEpochSecond) {
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmac(signingPayload(deviceId, expiresAtEpochSecond)));
        return TOKEN_VERSION + "." + expiresAtEpochSecond + "." + signature;
    }

    private String signingPayload(String deviceId, long expiresAtEpochSecond) {
        return TOKEN_VERSION + "." + ownerId + "." + deviceId + "." + expiresAtEpochSecond;
    }

    private byte[] hmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(enrollmentSecret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign owner device credential", ex);
        }
    }

    private String normalizeDeviceLabel(String requestedDeviceLabel) {
        String label = requestedDeviceLabel == null ? "device" : requestedDeviceLabel.trim();
        if (label.isEmpty()) label = "device";
        return label.length() <= MAX_DEVICE_LABEL_LENGTH
                ? label
                : label.substring(0, MAX_DEVICE_LABEL_LENGTH);
    }

    public record DeviceCredential(
            String ownerId,
            String deviceId,
            String deviceLabel,
            String token,
            Instant expiresAt) {}
}
