package ai.kuppa.planner;

import ai.kuppa.action.RiskLevel;

public record Plan(String response, String actionType, String actionSummary, String actionPayload, String reason, RiskLevel riskLevel) {
    public boolean hasAction() { return actionType != null && !actionType.isBlank(); }
}
