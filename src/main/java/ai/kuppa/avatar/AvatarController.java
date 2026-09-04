package ai.kuppa.avatar;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {
    private final AvatarAssetService avatarAssetService;

    public AvatarController(AvatarAssetService avatarAssetService) {
        this.avatarAssetService = avatarAssetService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return avatarAssetService.status();
    }

    @GetMapping(value = "/model", produces = "model/gltf-binary")
    public ResponseEntity<byte[]> model() {
        byte[] bytes = avatarAssetService.getAvatarBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=kuppa-avatar.glb")
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .contentType(MediaType.parseMediaType("model/gltf-binary"))
                .body(bytes);
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh() {
        avatarAssetService.ensureLocalAvatar(true);
        return avatarAssetService.status();
    }
}
