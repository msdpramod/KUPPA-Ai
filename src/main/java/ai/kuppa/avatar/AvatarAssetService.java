package ai.kuppa.avatar;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AvatarAssetService {
    private final HttpClient httpClient;
    private final URI sourceUri;
    private final Path dataDir;
    private final Path avatarFile;
    private volatile String lastError;

    public AvatarAssetService(
            @Value("${kuppa.avatar.source-url:https://models.readyplayer.me/KJIXZB.glb?quality=high&morphTargets=ARKit,Oculus%20Visemes&textureSizeLimit=1024}") String sourceUrl,
            @Value("${kuppa.avatar.data-dir:${user.home}/.kuppa-ai/avatar}") String dataDir) {
        this.sourceUri = URI.create(sourceUrl);
        this.dataDir = Path.of(dataDir).toAbsolutePath().normalize();
        this.avatarFile = this.dataDir.resolve("kuppa-avatar.glb");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
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

            Path temp = dataDir.resolve("kuppa-avatar.glb.part");
            Files.deleteIfExists(temp);

            HttpRequest request = HttpRequest.newBuilder(sourceUri)
                    .timeout(Duration.ofSeconds(60))
                    .header("User-Agent", "KUPPA-AI/1.0")
                    .GET()
                    .build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(temp));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Files.deleteIfExists(temp);
                throw new IllegalStateException("Avatar provider returned HTTP " + response.statusCode());
            }
            if (!isValidGlb(temp)) {
                Files.deleteIfExists(temp);
                throw new IllegalStateException("Downloaded avatar is not a valid GLB file");
            }
            Files.move(temp, avatarFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            lastError = null;
            return avatarFile;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Avatar download was interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store avatar locally: " + rootMessage(e), e);
        }
    }

    public byte[] getAvatarBytes() {
        try {
            Path file = ensureLocalAvatar(false);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read local avatar: " + rootMessage(e), e);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("source", sourceUri.toString());
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
