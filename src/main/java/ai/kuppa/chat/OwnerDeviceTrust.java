package ai.kuppa.chat;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "owner_device_trust", indexes = {
        @Index(name = "idx_owner_device_owner_revoked", columnList = "ownerId,revokedAt")
})
public class OwnerDeviceTrust {
    @Id
    private String deviceId;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false, length = 80)
    private String deviceLabel;

    @Column(nullable = false, length = 8)
    private String tokenVersion;

    @Column(nullable = false)
    private Instant enrolledAt;

    @Column
    private Instant lastContinuityIssuedAt;

    @Column
    private Instant revokedAt;

    @Column(nullable = false)
    private long continuityIssueCount;

    protected OwnerDeviceTrust() {}

    OwnerDeviceTrust(String ownerId, String deviceId, String deviceLabel, String tokenVersion, Instant enrolledAt) {
        this.ownerId = ownerId;
        this.deviceId = deviceId;
        this.deviceLabel = deviceLabel;
        this.tokenVersion = tokenVersion;
        this.enrolledAt = enrolledAt;
        this.continuityIssueCount = 0L;
    }

    boolean isActiveFor(String expectedOwnerId) {
        return revokedAt == null && ownerId.equals(expectedOwnerId);
    }

    void markContinuityIssued(Instant now) {
        this.lastContinuityIssuedAt = now;
        this.continuityIssueCount++;
    }

    void revoke(Instant now) {
        if (this.revokedAt == null) this.revokedAt = now;
    }

    public String getDeviceId() { return deviceId; }
    public String getOwnerId() { return ownerId; }
    public String getDeviceLabel() { return deviceLabel; }
    public String getTokenVersion() { return tokenVersion; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public Instant getLastContinuityIssuedAt() { return lastContinuityIssuedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public long getContinuityIssueCount() { return continuityIssueCount; }
}
