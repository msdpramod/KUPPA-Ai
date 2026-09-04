package ai.kuppa.planner;

import ai.kuppa.action.RiskLevel;
import ai.kuppa.conversation.VayuBrainGateway;
import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalPlanner implements Planner {
    private final VayuBrainGateway brainGateway;
    private final ActionIntentDetector actionIntentDetector;

    public LocalPlanner(VayuBrainGateway brainGateway, ActionIntentDetector actionIntentDetector) {
        this.brainGateway = brainGateway;
        this.actionIntentDetector = actionIntentDetector;
    }

    @Override
    public Plan plan(String message, List<PersonaMemory> memory) {
        return plan(message, memory, null, VayuBrainGateway.TurnContext.auto());
    }

    @Override
    public Plan plan(String message, List<PersonaMemory> memory, String correlationId) {
        return plan(message, memory, correlationId, VayuBrainGateway.TurnContext.auto());
    }

    @Override
    public Plan plan(String message, List<PersonaMemory> memory, String correlationId,
                     VayuBrainGateway.TurnContext turnContext) {
        ActionIntentDetector.Intent intent = actionIntentDetector.detect(message);

        if (intent == ActionIntentDetector.Intent.EXTERNAL_COMMUNICATION) {
            return new Plan(
                "I can prepare that. I created a proposed action and will not execute it until you approve.",
                "DRAFT_EXTERNAL_MESSAGE",
                "Prepare an external message for your review",
                message,
                "Your request explicitly asks KUPPA to communicate outside KUPPA AI.",
                RiskLevel.MEDIUM
            );
        }

        if (intent == ActionIntentDetector.Intent.HIGH_IMPACT) {
            return new Plan(
                "That can have significant consequences. I created a high-risk proposal; no action will occur without approval.",
                "HIGH_IMPACT_ACTION",
                "High-impact action requires explicit approval",
                message,
                "Your request explicitly asks KUPPA to perform a destructive or financial operation.",
                RiskLevel.HIGH
            );
        }

        VayuBrainGateway.Response brain = brainGateway.ask(message, memory, correlationId, turnContext);
        return new Plan(
            brain.message(),
            null, null, null, null, RiskLevel.LOW,
            brain
        );
    }
}
