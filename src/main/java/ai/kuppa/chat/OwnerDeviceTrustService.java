package ai.kuppa.chat;

import ai.kuppa.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class OwnerDeviceTrustService {
    private final OwnerDeviceTrustRepository repository;
    private final Clock clock;
    private final AuditService auditService;

    public OwnerDeviceTrustService(OwnerDeviceTrustRepository repository, AuditService auditService) {
        this(repository, Clock.systemUTC(), auditService);
    }

    OwnerDeviceTrustService(OwnerDeviceTrustRepository repository, Clock clock) {
        this(repository, clock, null);
    }

    OwnerDeviceTrustService(OwnerDeviceTrustRepository repository, Clock clock, AuditService auditService) {
        this.repository = repository;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Transactional
    public OwnerDeviceTrust register(OwnerDeviceIdentityService.DeviceCredential credential) {
        OwnerDeviceTrust trust = new OwnerDeviceTrust(
                credential.ownerId(),
                credential.deviceId(),
                credential.deviceLabel(),
                credential.tokenVersion(),
                clock.instant());
        OwnerDeviceTrust saved = repository.save(trust);
        audit("OWNER_DEVICE_ENROLLED", credential.deviceId(), "OWNER_ENROLLMENT", "EXPLICIT_ENROLLMENT");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DeviceSummary> inventory(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) return List.of();
        return repository.findByOwnerIdOrderByEnrolledAtDesc(ownerId).stream()
                .map(this::summary)
                .toList();
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
        audit("OWNER_DEVICE_MIGRATED", deviceId, "MIGRATION", "FIRST_VALIDATED_USE");
        return true;
    }

    @Transactional
    public boolean recordContinuityIssue(String ownerId, String deviceId) {
        OwnerDeviceTrust trust = repository.findById(deviceId).orElse(null);
        if (trust == null || !trust.isActiveFor(ownerId)) return false;
        trust.markContinuityIssued(clock.instant());
        repository.save(trust);
        audit("OWNER_DEVICE_CONTINUITY_ISSUED", deviceId, "DEVICE_POSSESSION", "OWNER_CONTINUITY_SESSION");
        return true;
    }

    @Transactional
    public boolean revoke(String ownerId, String deviceId) {
        OwnerDeviceTrust trust = repository.findById(deviceId).orElse(null);
        if (trust == null || !trust.isActiveFor(ownerId)) return false;
        trust.revoke(clock.instant());
        repository.save(trust);
        audit("OWNER_DEVICE_REVOKED_SELF", deviceId, "DEVICE_SELF", "SELF_REVOCATION");
        return true;
    }

    @Transactional
    public DeviceSummary remoteRevoke(String ownerId, String deviceId) {
        if (ownerId == null || ownerId.isBlank() || deviceId == null || deviceId.isBlank()) return null;
        OwnerDeviceTrust trust = repository.findById(deviceId).orElse(null);
        if (trust == null || !ownerId.equals(trust.getOwnerId())) return null;
        trust.revoke(clock.instant());
        repository.save(trust);
        audit("OWNER_DEVICE_REVOKED_REMOTE", deviceId, "OWNER_MANAGEMENT", "REMOTE_REVOCATION");
        return summary(trust);
    }

    private void audit(String eventType, String deviceId, String actor, String reason) {
        if (auditService == null) return;
        auditService.record(eventType, deviceId, "actor=" + actor + ";reason=" + reason);
    }

    private DeviceSummary summary(OwnerDeviceTrust trust) {
        return new DeviceSummary(
                trust.getDeviceId(),
                trust.getDeviceLabel(),
                trust.getTokenVersion(),
                trust.getEnrolledAt(),
                trust.getLastContinuityIssuedAt(),
                trust.getContinuityIssueCount(),
                trust.getRevokedAt(),
                trust.getRevokedAt() == null);
    }

    private String tokenVersion(String token) {
        if (token == null || token.isBlank()) return "unknown";
        int dot = token.indexOf('.');
        String version = dot > 0 ? token.substring(0, dot) : token;
        return version.length() <= 8 ? version : "unknown";
    }

    public record DeviceSummary(
            String deviceId,
            String deviceLabel,
            String tokenVersion,
            Instant enrolledAt,
            Instant lastContinuityIssuedAt,
            long continuityIssueCount,
            Instant revokedAt,
            boolean active) {}
}
