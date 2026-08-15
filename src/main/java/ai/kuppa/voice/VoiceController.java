package ai.kuppa.voice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {
    private final KuppaVoiceService voiceService;

    public VoiceController(KuppaVoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return voiceService.status();
    }

    @PostMapping("/repair")
    public Map<String, Object> repair() {
        return voiceService.repairVoice();
    }

    @PostMapping(value = "/synthesize", produces = "audio/wav")
    public ResponseEntity<byte[]> synthesize(@RequestBody VoiceRequest request) {
        byte[] audio = voiceService.synthesize(request.text());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }

    @ExceptionHandler(KuppaVoiceService.VoiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> unavailable(KuppaVoiceService.VoiceUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", e.getMessage()));
    }

    public record VoiceRequest(String text) {}
}
