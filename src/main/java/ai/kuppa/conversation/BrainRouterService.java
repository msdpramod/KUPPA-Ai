package ai.kuppa.conversation;

import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrainRouterService {
    private final OllamaConversationService ollama;
    private final OpenAiConversationService openAi;

    public BrainRouterService(OllamaConversationService ollama, OpenAiConversationService openAi) {
        this.ollama = ollama;
        this.openAi = openAi;
    }

    public String answer(String message, List<PersonaMemory> memory) {
        try {
            String local = ollama.answer(message, memory);
            if (local != null && !local.isBlank()) {
                return local;
            }
        } catch (Exception ignored) {
            // Fall through to hosted provider only when local Ollama is unavailable.
        }
        return openAi.answer(message, memory);
    }
}
