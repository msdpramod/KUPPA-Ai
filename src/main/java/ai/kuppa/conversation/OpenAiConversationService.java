package ai.kuppa.conversation;

import ai.kuppa.memory.MemoryPromptFormatter;
import ai.kuppa.memory.PersonaMemory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiConversationService {
    private static final URI RESPONSES_URI = URI.create("https://api.openai.com/v1/responses");

    private final ConversationContextService conversationContext;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final MemoryPromptFormatter memoryFormatter;

    public OpenAiConversationService(
            ConversationContextService conversationContext,
            ObjectMapper objectMapper,
            MemoryPromptFormatter memoryFormatter,
            @Value("${kuppa.openai.api-key:}") String configuredApiKey,
            @Value("${kuppa.openai.model:gpt-5-mini}") String model) {
        this.conversationContext = conversationContext;
        this.objectMapper = objectMapper;
        this.memoryFormatter = memoryFormatter;
        this.apiKey = resolveApiKey(configuredApiKey);
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    private String resolveApiKey(String configuredApiKey) {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) return configuredApiKey.trim();
        String environmentApiKey = System.getenv("OPENAI_API_KEY");
        return environmentApiKey == null ? "" : environmentApiKey.trim();
    }

    public boolean isConfigured() { return !apiKey.isBlank(); }

    public String answer(String currentMessage, List<PersonaMemory> memory) {
        return answer(currentMessage, memory, VayuBrainGateway.TurnContext.auto());
    }

    public String answer(String currentMessage, List<PersonaMemory> memory,
                         VayuBrainGateway.TurnContext turnContext) {
        if (!isConfigured()) {
            return "My voice loop is connected, but my conversational brain is not configured yet. Set OPENAI_API_KEY in the terminal that starts KUPPA AI, restart me, and then ask again.";
        }
        try {
            VayuBrainGateway.TurnContext normalized = turnContext == null
                    ? VayuBrainGateway.TurnContext.auto()
                    : turnContext.normalized();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("max_output_tokens", 900);
            body.put("instructions", buildInstructions(memory, normalized));
            body.put("input", buildConversationText(currentMessage, normalized));

            HttpRequest request = HttpRequest.newBuilder(RESPONSES_URI)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) return formatApiError(response.statusCode(), response.body());
            String text = extractOutputText(response.body());
            return text.isBlank() ? "I received your question, but the model returned no spoken answer." : text;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "My conversation was interrupted. Please say that again.";
        } catch (Exception e) {
            return "I hit a conversation error: " + e.getClass().getSimpleName() + ". Please try again.";
        }
    }

    private String buildInstructions(List<PersonaMemory> memory, VayuBrainGateway.TurnContext turnContext) {
        return "You are KUPPA AI, a private one-on-one personal assistant. Speak naturally and concisely, like a real conversation. " +
                "Answer the user's actual question directly. Use recent conversation only to resolve current context; do not treat it as permanent persona memory. " +
                turnContext.reasoningDirective() + " " +
                "Do not claim an external action happened. External actions must always go through KUPPA AI's approval system. " +
                "Use persona memory only when relevant. Treat tentative memory as a hypothesis rather than a fact and ask or hedge when it materially affects an answer.\n" +
                memoryFormatter.format(memory);
    }

    private String buildConversationText(String currentMessage, VayuBrainGateway.TurnContext turnContext) {
        StringBuilder transcript = new StringBuilder();
        for (ConversationContextService.ConversationTurn turn : conversationContext.recentTurns(currentMessage, turnContext)) {
            transcript.append("user".equals(turn.role()) ? "User: " : "KUPPA AI: ")
                    .append(turn.content())
                    .append('\n');
        }
        transcript.append("KUPPA AI:");
        return transcript.toString();
    }

    private String formatApiError(int statusCode, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText("");
            if (!message.isBlank()) return "OpenAI returned HTTP " + statusCode + ": " + message;
        } catch (Exception ignored) {}
        return "OpenAI returned HTTP " + statusCode + ". The request was rejected by the model API.";
    }

    private String extractOutputText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        StringBuilder answer = new StringBuilder();
        for (JsonNode item : root.path("output")) {
            for (JsonNode part : item.path("content")) {
                if ("output_text".equals(part.path("type").asText())) {
                    if (!answer.isEmpty()) answer.append('\n');
                    answer.append(part.path("text").asText());
                }
            }
        }
        return answer.toString().trim();
    }
}
