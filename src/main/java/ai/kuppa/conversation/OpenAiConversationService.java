package ai.kuppa.conversation;

import ai.kuppa.chat.ChatMessage;
import ai.kuppa.chat.ChatMessageRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiConversationService {
    private static final URI RESPONSES_URI = URI.create("https://api.openai.com/v1/responses");

    private final ChatMessageRepository chatRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public OpenAiConversationService(
            ChatMessageRepository chatRepository,
            ObjectMapper objectMapper,
            @Value("${kuppa.openai.api-key:}") String configuredApiKey,
            @Value("${kuppa.openai.model:gpt-5-mini}") String model) {
        this.chatRepository = chatRepository;
        this.objectMapper = objectMapper;
        this.apiKey = resolveApiKey(configuredApiKey);
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    private String resolveApiKey(String configuredApiKey) {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey.trim();
        }
        String environmentApiKey = System.getenv("OPENAI_API_KEY");
        return environmentApiKey == null ? "" : environmentApiKey.trim();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public String answer(String currentMessage, List<PersonaMemory> memory) {
        if (!isConfigured()) {
            return "My voice loop is connected, but my conversational brain is not configured yet. " +
                    "Set OPENAI_API_KEY in the terminal that starts KUPPA AI, restart me, and then ask again.";
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("max_output_tokens", 700);
            body.put("input", buildConversationInput(memory));

            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder(RESPONSES_URI)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "I could not reach my conversational model right now (HTTP " + response.statusCode() + "). " +
                        "Check OPENAI_API_KEY and try again.";
            }

            String text = extractOutputText(response.body());
            return text.isBlank() ? "I received your question, but the model returned no spoken answer." : text;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "My conversation was interrupted. Please say that again.";
        } catch (Exception e) {
            return "I hit a conversation error: " + e.getClass().getSimpleName() + ". Please try again.";
        }
    }

    private List<Map<String, Object>> buildConversationInput(List<PersonaMemory> memory) {
        List<Map<String, Object>> input = new ArrayList<>();
        String persona = memory.stream()
                .limit(20)
                .map(m -> m.getCategory() + ": " + m.getContent())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No confirmed persona memory yet.");

        input.add(message("developer", "You are KUPPA AI, a private one-on-one personal assistant. " +
                "Speak naturally and concisely, like a real conversation. Answer the user's actual question directly. " +
                "Do not claim an external action happened. External actions must always go through KUPPA AI's approval system. " +
                "Use these persona memories only when relevant:\n" + persona));

        List<ChatMessage> recent = new ArrayList<>(chatRepository.findTop50ByOrderByCreatedAtDesc());
        if (recent.size() > 14) recent = new ArrayList<>(recent.subList(0, 14));
        Collections.reverse(recent);
        for (ChatMessage item : recent) {
            String role = "USER".equals(item.getRole()) ? "user" : "assistant";
            input.add(message(role, item.getContent()));
        }
        return input;
    }

    private Map<String, Object> message(String role, String text) {
        return Map.of(
                "role", role,
                "content", List.of(Map.of("type", "input_text", "text", text))
        );
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
