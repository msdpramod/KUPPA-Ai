package ai.kuppa.planner;

import ai.kuppa.conversation.VayuBrainGateway;
import ai.kuppa.memory.PersonaMemory;
import java.util.List;

public interface Planner {
    Plan plan(String message, List<PersonaMemory> memory);

    default Plan plan(String message, List<PersonaMemory> memory, String correlationId) {
        return plan(message, memory);
    }

    default Plan plan(String message, List<PersonaMemory> memory, String correlationId,
                      VayuBrainGateway.TurnContext turnContext) {
        return plan(message, memory, correlationId);
    }
}
