package ai.kuppa.voice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class KuppaVoiceService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI synthesizeUri;
    private final URI infoUri;
    private final String voice;
    private final double lengthScale;
    private final double noiseScale;
    private final double noiseWScale;
    private final boolean enabled;
    private final boolean autoStart;
    private final String pythonCommand;
    private final int serverPort;
    private volatile Process piperProcess;
    private volatile String lastStartupError;

    public KuppaVoiceService(
            ObjectMapper objectMapper,
            @Value("${kuppa.voice.base-url:http://localhost:5000}") String baseUrl,
            @Value("${kuppa.voice.voice:en_US-amy-medium}") String voice,
            @Value("${kuppa.voice.length-scale:1.04}") double lengthScale,
            @Value("${kuppa.voice.noise-scale:0.55}") double noiseScale,
            @Value("${kuppa.voice.noise-w-scale:0.72}") double noiseWScale,
            @Value("${kuppa.voice.enabled:true}") boolean enabled,
            @Value("${kuppa.voice.auto-start:true}") boolean autoStart,
            @Value("${kuppa.voice.python-command:python3}") String pythonCommand,
            @Value("${kuppa.voice.port:5000}") int serverPort) {
        this.objectMapper = objectMapper;
        this.voice = voice;
        this.lengthScale = lengthScale;
        this.noiseScale = noiseScale;
        this.noiseWScale = noiseWScale;
        this.enabled = enabled;
        this.autoStart = autoStart;
        this.pythonCommand = pythonCommand;
        this.serverPort = serverPort;
        String root = baseUrl.replaceAll("/$", "");
        this.synthesizeUri = URI.create(root + "/synthesize");
        this.infoUri = URI.create(root + "/info");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @PostConstruct
    public void warmUpOnStartup() {
        if (!enabled || !autoStart) return;
        Thread starter = new Thread(() -> {
            try {
                ensureVoiceEngine();
            } catch (Exception e) {
                lastStartupError = rootMessage(e);
            }
        }, "kuppa-voice-warmup");
        starter.setDaemon(true);
        starter.start();
    }

    public byte[] synthesize(String text) {
        if (!enabled) {
            throw new VoiceUnavailableException("KUPPA neural voice is disabled by configuration");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text is required");
        }

        ensureVoiceEngine();

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
                String details = new String(response.body(), StandardCharsets.UTF_8).trim();
                if (details.length() > 300) details = details.substring(0, 300);
                throw new VoiceUnavailableException("Piper returned HTTP " + response.statusCode() +
                        (details.isBlank() ? "" : ": " + details));
            }
            if (response.body() == null || response.body().length < 44) {
                throw new VoiceUnavailableException("Piper returned an empty or invalid WAV response");
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VoiceUnavailableException("Local KUPPA voice synthesis was interrupted", e);
        } catch (VoiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new VoiceUnavailableException("Cannot reach Piper at " + synthesizeUri + ": " + rootMessage(e), e);
        }
    }

    private void ensureVoiceEngine() {
        if (!enabled) {
            throw new VoiceUnavailableException("KUPPA neural voice is disabled by configuration");
        }
        if (isReachable()) return;
        if (!autoStart) {
            throw new VoiceUnavailableException("Piper is offline at " + infoUri + " and auto-start is disabled");
        }
        startPiperIfNeeded();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            if (isReachable()) return;
            if (piperProcess != null && !piperProcess.isAlive()) break;
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new VoiceUnavailableException("Interrupted while waiting for Piper to start", e);
            }
        }
        String reason = lastStartupError == null || lastStartupError.isBlank()
                ? "Piper did not become healthy on port " + serverPort
                : lastStartupError;
        throw new VoiceUnavailableException("KUPPA could not start the local neural voice engine: " + reason);
    }

    private synchronized void startPiperIfNeeded() {
        if (isReachable()) return;
        if (piperProcess != null && piperProcess.isAlive()) return;

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonCommand,
                    "-m", "piper.http_server",
                    "-m", voice,
                    "--port", String.valueOf(serverPort)
            );
            pb.redirectErrorStream(true);
            piperProcess = pb.start();
            lastStartupError = null;

            Thread logReader = new Thread(() -> captureProcessOutput(piperProcess), "kuppa-piper-log");
            logReader.setDaemon(true);
            logReader.start();
        } catch (Exception e) {
            lastStartupError = rootMessage(e);
            throw new VoiceUnavailableException(
                    "Unable to launch Piper with '" + pythonCommand + " -m piper.http_server'. " +
                            "Make sure piper-tts[http] and voice '" + voice + "' are installed. Cause: " + lastStartupError,
                    e
            );
        }
    }

    private void captureProcessOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder recent = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (recent.length() > 1200) recent.delete(0, Math.min(600, recent.length()));
                recent.append(line).append('\n');
                lastStartupError = recent.toString().trim();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isReachable() {
        try {
            HttpRequest request = HttpRequest.newBuilder(infoUri)
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("configuredVoice", voice);
        result.put("endpoint", synthesizeUri.toString());
        result.put("autoStart", autoStart);
        result.put("pythonCommand", pythonCommand);
        result.put("managedProcessAlive", piperProcess != null && piperProcess.isAlive());
        try {
            HttpRequest request = HttpRequest.newBuilder(infoUri)
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            result.put("reachable", response.statusCode() >= 200 && response.statusCode() < 300);
            result.put("httpStatus", response.statusCode());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    JsonNode info = objectMapper.readTree(response.body());
                    result.put("piperInfo", info);
                } catch (Exception ignored) {
                    result.put("piperInfo", response.body());
                }
            } else {
                result.put("error", response.body());
            }
        } catch (Exception e) {
            result.put("reachable", false);
            result.put("error", rootMessage(e));
            if (lastStartupError != null && !lastStartupError.isBlank()) {
                result.put("startupError", lastStartupError);
            }
        }
        return result;
    }

    @PreDestroy
    public void stopManagedPiper() {
        if (piperProcess != null && piperProcess.isAlive()) {
            piperProcess.destroy();
        }
    }

    private String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
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
