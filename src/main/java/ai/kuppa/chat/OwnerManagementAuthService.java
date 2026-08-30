package ai.kuppa.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class OwnerManagementAuthService {
    private static final int MIN_SECRET_LENGTH = 32;

    private final byte[] managementSecret;

    public OwnerManagementAuthService(
            @Value("${kuppa.identity.management-secret:}") String managementSecret) {
        this.managementSecret = bytes(managementSecret);
    }

    public boolean enabled() {
        return managementSecret.length >= MIN_SECRET_LENGTH;
    }

    public boolean authorize(String presentedSecret) {
        if (!enabled() || presentedSecret == null) return false;
        return MessageDigest.isEqual(
                managementSecret,
                presentedSecret.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] bytes(String value) {
        return value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
    }
}
