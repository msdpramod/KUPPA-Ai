package ai.kuppa.avatar;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AvatarAssetService {
    private final HttpClient httpClient;
    private final URI configuredSourceUri;
    private final Path dataDir;
    private final Path avatarFile;
    private volatile String lastError;
    private volatile String activeSource;
    private volatile String activeDownloader;

    public AvatarAssetService(
            @Value("${kuppa.avatar.source-url:https://models.readyplayer.me/KJIXZB.glb}") String sourceUrl,
            @Value("${kuppa.avatar.data-dir:${user.home}/.kuppa-ai/avatar}") String dataDir) {
        this.configuredSourceUri = URI.create(sourceUrl);
        this.dataDir = Path.of(dataDir).toAbsolutePath().normalize();
        this.avatarFile = this.dataDir.resolve("kuppa-avatar.glb");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @PostConstruct
    public void warmUp() {
        Thread t = new Thread(() -> {
            try { ensureLocalAvatar(false); }
            catch (Exception e) { lastError = rootMessage(e); }
        }, "kuppa-avatar-warmup");
        t.setDaemon(true);
        t.start();
    }

    public synchronized Path ensureLocalAvatar(boolean force) {
        try {
            Files.createDirectories(dataDir);
            if (!force && isValidGlb(avatarFile)) return avatarFile;

            List<URI> candidates = sourceCandidates();
            List<String> failures = new ArrayList<>();
            for (URI candidate : candidates) {
                Path temp = dataDir.resolve("kuppa-avatar.glb.part");
                Files.deleteIfExists(temp);
                try {
                    String downloader = downloadCandidate(candidate, temp);
                    if (!isValidGlb(temp)) {
                        failures.add(candidate + " -> downloaded by " + downloader + " but response was not a valid GLB");
                        Files.deleteIfExists(temp);
                        continue;
                    }
                    try {
                        Files.move(temp, avatarFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                        Files.move(temp, avatarFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    activeSource = candidate.toString();
                    activeDownloader = downloader;
                    lastError = null;
                    return avatarFile;
                } catch (Exception e) {
                    if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                    Files.deleteIfExists(temp);
                    failures.add(candidate + " -> " + rootMessage(e));
                }
            }
            lastError = String.join(" | ", failures);
            throw new IllegalStateException("Unable to download a valid Ready Player Me avatar. " + lastError);
        } catch (IOException e) {
            lastError = "Unable to store avatar locally: " + rootMessage(e);
            throw new IllegalStateException(lastError, e);
        }
    }

    private String downloadCandidate(URI candidate, Path temp) throws Exception {
        try {
            downloadWithJava(candidate, temp);
            return "java-http-client";
        } catch (Exception javaError) {
            Files.deleteIfExists(temp);
            try {
                downloadWithCurl(candidate, temp);
                return "curl";
            } catch (Exception curlError) {
                throw new IllegalStateException(
                        "Java download failed: " + rootMessage(javaError)
                                + "; curl fallback failed: " + rootMessage(curlError),
                        curlError);
            }
        }
    }

    private void downloadWithJava(URI candidate, Path temp) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(candidate)
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "KUPPA-AI/1.0")
                .header("Accept", "model/gltf-binary,application/octet-stream,*/*")
                .GET()
                .build();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(temp));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
    }

    private void downloadWithCurl(URI candidate, Path temp) throws Exception {
        Process process = new ProcessBuilder(
                "curl",
                "-fL",
                "--connect-timeout", "10",
                "--max-time", "60",
                "--retry", "2",
                "-A", "KUPPA-AI/1.0",
                "-H", "Accept: model/gltf-binary,application/octet-stream,*/*",
                "-o", temp.toString(),
                candidate.toString())
                .redirectErrorStream(true)
                .start();

        boolean finished = process.waitFor(70, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("curl timed out");
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.exitValue() != 0) {
            throw new IllegalStateException("curl exit " + process.exitValue() + (output.isBlank() ? "" : ": " + output));
        }
    }

    private List<URI> sourceCandidates() {
        List<URI> candidates = new ArrayList<>();
        candidates.add(configuredSourceUri);
        addUnique(candidates, URI.create("https://models.readyplayer.me/KJIXZB.glb"));
        addUnique(candidates, URI.create("https://avatars.readyplayer.me/KJIXZB.glb"));
        return candidates;
    }

    private void addUnique(List<URI> values, URI candidate) {
        if (!values.contains(candidate)) values.add(candidate);
    }

    public byte[] getAvatarBytes() {
        try {
            Path file = ensureLocalAvatar(false);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            lastError = "Unable to read local avatar: " + rootMessage(e);
            throw new IllegalStateException(lastError, e);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("configuredSource", configuredSourceUri.toString());
        out.put("activeSource", activeSource == null ? "" : activeSource);
        out.put("downloader", activeDownloader == null ? "" : activeDownloader);
        out.put("localPath", avatarFile.toString());
        out.put("cached", isValidGlb(avatarFile));
        try { if (Files.exists(avatarFile)) out.put("sizeBytes", Files.size(avatarFile)); }
        catch (IOException ignored) { }
        if (lastError != null && !lastError.isBlank()) out.put("error", lastError);
        return out;
    }

    private boolean isValidGlb(Path path) {
        try {
            if (!Files.exists(path) || Files.size(path) < 100_000) return false;
            byte[] magic = new byte[4];
            try (var in = Files.newInputStream(path)) {
                if (in.read(magic) != 4) return false;
            }
            return magic[0] == 'g' && magic[1] == 'l' && magic[2] == 'T' && magic[3] == 'F';
        } catch (IOException e) {
            return false;
        }
    }

    private String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null) current = current.getCause();
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
