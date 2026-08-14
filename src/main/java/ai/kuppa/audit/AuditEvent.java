package ai.kuppa.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String eventType;
    private String actionId;
    @Column(nullable = false, length = 8000) private String detail;
    @Column(nullable = false) private Instant createdAt;

    protected AuditEvent() {}
    public AuditEvent(String eventType, String actionId, String detail) {
        this.eventType = eventType;
        this.actionId = actionId;
        this.detail = detail;
        this.createdAt = Instant.now();
    }
    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActionId() { return actionId; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
