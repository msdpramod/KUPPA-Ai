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
        try {
            String local = ollama.answer(message, memory);
            if (local != null && !local.isBlank()) return local;
            throw new IllegalStateException("Ollama returned an empty response");
        } catch (Exception ollamaError) {
            if (openAiFallbackEnabled) {
                return openAi.answer(message, memory);
            }
            String reason = rootMessage(ollamaError);
            return "My local Ollama brain is unavailable right now. Make sure Ollama is running and the configured model is installed. Details: " + reason;
        }
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
