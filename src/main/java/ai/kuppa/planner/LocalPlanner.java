package ai.kuppa.planner;

import ai.kuppa.action.RiskLevel;
import ai.kuppa.conversation.BrainRouterService;
import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class LocalPlanner implements Planner {
    private final BrainRouterService brainRouter;

    public LocalPlanner(BrainRouterService brainRouter) {
        this.brainRouter = brainRouter;
    }

    @Override
    public Plan plan(String message, List<PersonaMemory> memory) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("send") || lower.contains("email") || lower.contains("reply") || lower.contains("post")) {
            return new Plan(
                "I can prepare that. I created a proposed action and will not execute it until you approve.",
                "DRAFT_EXTERNAL_MESSAGE",
                "Prepare an external message for your review",
                message,
                "Your request appears to involve communication outside KUPPA AI.",
                RiskLevel.MEDIUM
            );
        }
        if (lower.contains("delete") || lower.contains("pay") || lower.contains("purchase") || lower.contains("transfer")) {
            return new Plan(
                "That can have significant consequences. I created a high-risk proposal; no action will occur without approval.",
                "HIGH_IMPACT_ACTION",
                "High-impact action requires explicit approval",
                message,
                "The requested operation may be destructive or financial.",
                RiskLevel.HIGH
            );
        }

        return new Plan(
            brainRouter.answer(message, memory),
            null, null, null, null, RiskLevel.LOW
        );
    }
}
