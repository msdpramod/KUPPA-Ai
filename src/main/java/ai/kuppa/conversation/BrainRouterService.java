package ai.kuppa.conversation;

import ai.kuppa.memory.PersonaMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrainRouterService {
    private final OllamaConversationService ollama;
    private final OpenAiConversationService openAi;
    private final boolean openAiFallbackEnabled;

    public BrainRouterService(
            OllamaConversationService ollama,
            OpenAiConversationService openAi,
            @Value("${kuppa.openai.fallback-enabled:false}") boolean openAiFallbackEnabled) {
        this.ollama = ollama;
        this.openAi = openAi;
        this.openAiFallbackEnabled = openAiFallbackEnabled;
    }

    public String answer(String message, List<PersonaMemory> memory) {
        return answerDetailed(message, memory).message();
    }

    public BrainAnswer answerDetailed(String message, List<PersonaMemory> memory) {
        return answerDetailed(message, memory, VayuBrainGateway.TurnContext.auto());
    }

    public BrainAnswer answerDetailed(String message, List<PersonaMemory> memory,
                                      VayuBrainGateway.TurnContext turnContext) {
        VayuBrainGateway.TurnContext normalized = turnContext == null
                ? VayuBrainGateway.TurnContext.auto()
                : turnContext.normalized();
        try {
            String local = ollama.answer(message, memory, normalized);
            if (local != null && !local.isBlank()) {
                return new BrainAnswer(local, "OLLAMA", false, null);
            }
            throw new IllegalStateException("Ollama returned an empty response");
        } catch (Exception ollamaError) {
            if (openAiFallbackEnabled) {
                try {
                    String fallback = openAi.answer(message, memory, normalized);
                    if (fallback != null && !fallback.isBlank()) {
                        return new BrainAnswer(fallback, "OPENAI_FALLBACK", true, "OLLAMA_UNAVAILABLE");
                    }
                } catch (Exception ignored) {
                    // The gateway exposes a stable degraded state instead of leaking provider exception details.
                }
            }
            return new BrainAnswer(
                    "I’m here, but Vayu’s reasoning service is temporarily unavailable. I can still stay with the conversation, but I won’t pretend I completed brain-level reasoning. Please try again shortly.",
                    "NONE",
                    true,
                    "VAYU_UNAVAILABLE"
            );
        }
    }

    public record BrainAnswer(String message, String provider, boolean degraded, String errorCode) {}
}
