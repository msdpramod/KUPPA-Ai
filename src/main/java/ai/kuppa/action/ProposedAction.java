package ai.kuppa.action;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "proposed_actions")
public class ProposedAction {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String actionType;
    @Column(nullable = false, length = 4000) private String summary;
    @Column(nullable = false, length = 8000) private String payload;
    @Column(nullable = false, length = 4000) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RiskLevel riskLevel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ActionStatus status;
    @Column(nullable = false) private Instant createdAt;
    private Instant approvedAt;
    private Instant executedAt;
    @Column(length = 4000) private String executionResult;

    protected ProposedAction() {}

    public ProposedAction(String actionType, String summary, String payload, String reason, RiskLevel riskLevel) {
        this.actionType = actionType;
        this.summary = summary;
        this.payload = payload;
        this.reason = reason;
        this.riskLevel = riskLevel;
        this.status = ActionStatus.PENDING_APPROVAL;
        this.createdAt = Instant.now();
    }

    public void approve() {
        if (status != ActionStatus.PENDING_APPROVAL) throw new IllegalStateException("Only pending actions can be approved");
        status = ActionStatus.APPROVED;
        approvedAt = Instant.now();
    }

    public void reject() {
        if (status != ActionStatus.PENDING_APPROVAL) throw new IllegalStateException("Only pending actions can be rejected");
        status = ActionStatus.REJECTED;
    }

    public void markExecuted(String result) {
        if (status != ActionStatus.APPROVED) throw new IllegalStateException("KUPPA AI refuses to execute an unapproved action");
        status = ActionStatus.EXECUTED;
        executedAt = Instant.now();
        executionResult = result;
    }

    public void markFailed(String result) {
        if (status != ActionStatus.APPROVED) throw new IllegalStateException("Unapproved actions cannot enter execution");
        status = ActionStatus.FAILED;
        executionResult = result;
    }

    public String getId() { return id; }
    public String getActionType() { return actionType; }
    public String getSummary() { return summary; }
    public String getPayload() { return payload; }
    public String getReason() { return reason; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public ActionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getExecutedAt() { return executedAt; }
    public String getExecutionResult() { return executionResult; }
}
