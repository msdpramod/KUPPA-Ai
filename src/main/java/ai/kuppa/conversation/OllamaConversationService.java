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
public class OllamaConversationService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI chatUri;
    private final String model;
    private final MemoryPromptFormatter memoryFormatter;

    public OllamaConversationService(
            ObjectMapper objectMapper,
            MemoryPromptFormatter memoryFormatter,
            @Value("${kuppa.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${kuppa.ollama.model:llama3.2}") String model) {
        this.objectMapper = objectMapper;
        this.memoryFormatter = memoryFormatter;
        this.model = model;
        this.chatUri = URI.create(baseUrl.replaceAll("/$", "") + "/api/chat");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public String answer(String currentMessage, List<PersonaMemory> memory) throws Exception {
        String persona = memoryFormatter.format(memory);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are KUPPA AI, a private one-on-one personal assistant. Speak naturally, clearly, and concisely. " +
                        "Answer the user's actual question directly. Never claim an external action happened unless it passed KUPPA AI's approval system. " +
                        "Use persona memory only when relevant. Treat tentative memory as a hypothesis, not a fact, and ask or hedge when it materially affects an answer.\n" + persona),
                Map.of("role", "user", "content", currentMessage)
        ));

        HttpRequest request = HttpRequest.newBuilder(chatUri)
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Ollama HTTP " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("message").path("content").asText("").trim();
    }
}
