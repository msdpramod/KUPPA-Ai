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
public class OwnerDeviceIdentityService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_LENGTH = 32;
    private static final int MAX_DEVICE_LABEL_LENGTH = 80;
    private static final String LEGACY_TOKEN_VERSION = "v1";
    private static final String DEDICATED_TOKEN_VERSION = "v2";

    private final String ownerId;
    private final byte[] enrollmentSecret;
    private final byte[] deviceSigningSecret;
    private final byte[] previousDeviceSigningSecret;
    private final long tokenTtlSeconds;
    private final Clock clock;

    @Autowired
    public OwnerDeviceIdentityService(
            @Value("${kuppa.identity.owner-id:owner}") String ownerId,
            @Value("${kuppa.identity.enrollment-secret:}") String enrollmentSecret,
            @Value("${kuppa.identity.device-signing-secret:}") String deviceSigningSecret,
            @Value("${kuppa.identity.previous-device-signing-secret:}") String previousDeviceSigningSecret,
            @Value("${kuppa.identity.device-token-ttl-seconds:7776000}") long tokenTtlSeconds) {
        this(ownerId, enrollmentSecret, deviceSigningSecret, previousDeviceSigningSecret,
                tokenTtlSeconds, Clock.systemUTC());
    }

    OwnerDeviceIdentityService(String ownerId, String enrollmentSecret, String deviceSigningSecret,
                               String previousDeviceSigningSecret, long tokenTtlSeconds, Clock clock) {
        String normalizedOwnerId = ownerId == null ? "" : ownerId.trim();
        if (normalizedOwnerId.isEmpty()) throw new IllegalArgumentException("Owner id must not be blank");
        if (tokenTtlSeconds <= 0) throw new IllegalArgumentException("Device token TTL must be positive");
        this.ownerId = normalizedOwnerId;
        this.enrollmentSecret = bytes(enrollmentSecret);
        this.deviceSigningSecret = bytes(deviceSigningSecret);
        this.previousDeviceSigningSecret = bytes(previousDeviceSigningSecret);
        this.tokenTtlSeconds = tokenTtlSeconds;
        this.clock = clock;
    }

    public boolean enabled() {
        if (enrollmentSecret.length < MIN_SECRET_LENGTH) return false;
        boolean dedicatedConfigured = deviceSigningSecret.length > 0 || previousDeviceSigningSecret.length > 0;
        if (!dedicatedConfigured) return true;
        if (deviceSigningSecret.length < MIN_SECRET_LENGTH) return false;
        return previousDeviceSigningSecret.length == 0
                || previousDeviceSigningSecret.length >= MIN_SECRET_LENGTH;
    }

    public String ownerId() {
        return ownerId;
    }

    public String signingMode() {
        return usesDedicatedSigning() ? "DEDICATED_V2" : "LEGACY_V1";
    }

    public DeviceCredential enroll(String presentedEnrollmentSecret, String requestedDeviceLabel) {
        if (!enabled()) {
            throw new IllegalStateException("Owner device enrollment is not configured safely");
        }
        if (!matchesEnrollmentSecret(presentedEnrollmentSecret)) {
            throw new SecurityException("Owner enrollment credential rejected");
        }

        String deviceId = UUID.randomUUID().toString();
        String deviceLabel = normalizeDeviceLabel(requestedDeviceLabel);
        Instant expiresAt = clock.instant().plusSeconds(tokenTtlSeconds);
        String version = usesDedicatedSigning() ? DEDICATED_TOKEN_VERSION : LEGACY_TOKEN_VERSION;
        String token = tokenFor(version, deviceId, expiresAt.getEpochSecond());
        return new DeviceCredential(ownerId, deviceId, deviceLabel, version, token, expiresAt);
    }

    public boolean validate(String deviceId, String token) {
        if (!enabled() || deviceId == null || deviceId.isBlank() || token == null) return false;

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) return false;
        String version = parts[0];
        if (!LEGACY_TOKEN_VERSION.equals(version) && !DEDICATED_TOKEN_VERSION.equals(version)) return false;
        try {
            long expiresAtEpochSecond = Long.parseLong(parts[1]);
            if (expiresAtEpochSecond <= clock.instant().getEpochSecond()) return false;
            byte[] supplied = Base64.getUrlDecoder().decode(parts[2]);
            String payload = signingPayload(version, deviceId, expiresAtEpochSecond);
            if (LEGACY_TOKEN_VERSION.equals(version)) {
                return MessageDigest.isEqual(hmac(enrollmentSecret, payload), supplied);
            }
            if (!usesDedicatedSigning()) return false;
            if (MessageDigest.isEqual(hmac(deviceSigningSecret, payload), supplied)) return true;
            return previousDeviceSigningSecret.length >= MIN_SECRET_LENGTH
                    && MessageDigest.isEqual(hmac(previousDeviceSigningSecret, payload), supplied);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean usesDedicatedSigning() {
        return deviceSigningSecret.length >= MIN_SECRET_LENGTH;
    }

    private boolean matchesEnrollmentSecret(String presentedEnrollmentSecret) {
        if (presentedEnrollmentSecret == null) return false;
        return MessageDigest.isEqual(
                enrollmentSecret,
                presentedEnrollmentSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String tokenFor(String version, String deviceId, long expiresAtEpochSecond) {
        byte[] signingKey = DEDICATED_TOKEN_VERSION.equals(version) ? deviceSigningSecret : enrollmentSecret;
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmac(signingKey, signingPayload(version, deviceId, expiresAtEpochSecond)));
        return version + "." + expiresAtEpochSecond + "." + signature;
    }

    private String signingPayload(String version, String deviceId, long expiresAtEpochSecond) {
        return version + "." + ownerId + "." + deviceId + "." + expiresAtEpochSecond;
    }

    private byte[] hmac(byte[] secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign owner device credential", ex);
        }
    }

    private byte[] bytes(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
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
            String tokenVersion,
            String token,
            Instant expiresAt) {}
}
