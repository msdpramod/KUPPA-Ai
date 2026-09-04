package ai.kuppa.planner;

import ai.kuppa.action.RiskLevel;
import ai.kuppa.conversation.VayuBrainGateway;

public record Plan(String response, String actionType, String actionSummary, String actionPayload,
                   String reason, RiskLevel riskLevel, VayuBrainGateway.Response brain) {

    public Plan(String response, String actionType, String actionSummary, String actionPayload,
                String reason, RiskLevel riskLevel) {
        this(response, actionType, actionSummary, actionPayload, reason, riskLevel, null);
    }

    public boolean hasAction() { return actionType != null && !actionType.isBlank(); }
}
