package ai.kuppa.chat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class OwnerDeviceTrustService {
    private final OwnerDeviceTrustRepository repository;
    private final Clock clock;

    public OwnerDeviceTrustService(OwnerDeviceTrustRepository repository) {
        this(repository, Clock.systemUTC());
    }

    OwnerDeviceTrustService(OwnerDeviceTrustRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public OwnerDeviceTrust register(OwnerDeviceIdentityService.DeviceCredential credential) {
        OwnerDeviceTrust trust = new OwnerDeviceTrust(
                credential.ownerId(),
                credential.deviceId(),
                credential.deviceLabel(),
                credential.tokenVersion(),
                clock.instant());
        return repository.save(trust);
    }

    @Transactional
    public boolean authorizeValidatedCredential(String ownerId, String deviceId, String token) {
        if (ownerId == null || deviceId == null || deviceId.isBlank()) return false;

        OwnerDeviceTrust existing = repository.findById(deviceId).orElse(null);
        if (existing != null) return existing.isActiveFor(ownerId);

        // Migration path for device credentials issued before the persistent trust registry existed.
        // Callers must cryptographically validate the device token before invoking this method.
        OwnerDeviceTrust migrated = new OwnerDeviceTrust(
                ownerId,
                deviceId,
                "migrated-device",
                tokenVersion(token),
                clock.instant());
        repository.save(migrated);
        return true;
    }

    @Transactional
    public boolean recordContinuityIssue(String ownerId, String deviceId) {
        OwnerDeviceTrust trust = repository.findById(deviceId).orElse(null);
        if (trust == null || !trust.isActiveFor(ownerId)) return false;
        trust.markContinuityIssued(clock.instant());
        repository.save(trust);
        return true;
    }

    @Transactional
    public boolean revoke(String ownerId, String deviceId) {
        OwnerDeviceTrust trust = repository.findById(deviceId).orElse(null);
        if (trust == null || !trust.isActiveFor(ownerId)) return false;
        trust.revoke(clock.instant());
        repository.save(trust);
        return true;
    }

    private String tokenVersion(String token) {
        if (token == null || token.isBlank()) return "unknown";
        int dot = token.indexOf('.');
        String version = dot > 0 ? token.substring(0, dot) : token;
        return version.length() <= 8 ? version : "unknown";
    }
}
