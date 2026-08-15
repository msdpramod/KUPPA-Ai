package ai.kuppa.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class KuppaVoiceService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI synthesizeUri;
    private final String voice;
    private final double lengthScale;
    private final double noiseScale;
    private final double noiseWScale;

    public KuppaVoiceService(
            ObjectMapper objectMapper,
            @Value("${kuppa.voice.base-url:http://localhost:5000}") String baseUrl,
            @Value("${kuppa.voice.voice:en_US-amy-medium}") String voice,
            @Value("${kuppa.voice.length-scale:1.04}") double lengthScale,
            @Value("${kuppa.voice.noise-scale:0.55}") double noiseScale,
            @Value("${kuppa.voice.noise-w-scale:0.72}") double noiseWScale) {
        this.objectMapper = objectMapper;
        this.voice = voice;
        this.lengthScale = lengthScale;
        this.noiseScale = noiseScale;
        this.noiseWScale = noiseWScale;
        this.synthesizeUri = URI.create(baseUrl.replaceAll("/$", "") + "/synthesize");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public byte[] synthesize(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text is required");
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("text", normalizeForSpeech(text));
            body.put("voice", voice);
            body.put("length_scale", lengthScale);
            body.put("noise_scale", noiseScale);
            body.put("noise_w_scale", noiseWScale);

            HttpRequest request = HttpRequest.newBuilder(synthesizeUri)
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new VoiceUnavailableException("Local KUPPA voice engine returned HTTP " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VoiceUnavailableException("Local KUPPA voice synthesis was interrupted", e);
        } catch (VoiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new VoiceUnavailableException("Local KUPPA voice engine is unavailable", e);
        }
    }

    private String normalizeForSpeech(String text) {
        String cleaned = text
                .replace("KUPPA AI", "Kuppa")
                .replace("KUPPA", "Kuppa")
                .replaceAll("https?://\\S+", "")
                .replaceAll("[`*_#]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned.length() > 1800 ? cleaned.substring(0, 1800) : cleaned;
    }

    public static class VoiceUnavailableException extends RuntimeException {
        public VoiceUnavailableException(String message) { super(message); }
        public VoiceUnavailableException(String message, Throwable cause) { super(message, cause); }
    }
}
