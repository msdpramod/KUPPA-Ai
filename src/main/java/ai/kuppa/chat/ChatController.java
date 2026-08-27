package ai.kuppa.chat;

import ai.kuppa.conversation.VayuBrainGateway;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;
    private final VayuBrainGateway brainGateway;
    private final ChatContinuityService continuityService;
    private final ContinuitySessionService continuitySessionService;

    public ChatController(ChatService service, VayuBrainGateway brainGateway,
                          ChatContinuityService continuityService,
                          ContinuitySessionService continuitySessionService) {
        this.service = service;
        this.brainGateway = brainGateway;
        this.continuityService = continuityService;
        this.continuitySessionService = continuitySessionService;
    }

    @PostMapping
    public ChatService.ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(
                request.message(),
                request.correlationId(),
                request.turnMode(),
                request.parentCorrelationId(),
                request.clientSessionId());
    }

    @PostMapping("/session")
    public ContinuitySessionService.SessionCredential createContinuitySession() {
        if (!continuitySessionService.enabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Secure continuity sessions require KUPPA_CONTINUITY_SIGNING_SECRET");
        }
        return continuitySessionService.issue();
    }

    @GetMapping("/resumable")
    public ChatContinuityService.ResumableTurn resumable(@RequestParam String clientSessionId) {
        return continuityService.latest(clientSessionId);
    }

    @GetMapping("/resumable/secure")
    public ChatContinuityService.ResumableTurn secureResumable(
            @RequestParam String clientSessionId,
            @RequestHeader(value = "X-KUPPA-Continuity-Token", required = false) String token) {
        if (!continuitySessionService.enabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Secure continuity sessions are disabled");
        }
        if (!continuitySessionService.validate(clientSessionId, token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired continuity credential");
        }
        return continuityService.latest(clientSessionId);
    }

    @PostMapping("/{correlationId}/cancel")
    public VayuBrainGateway.Cancellation cancel(@PathVariable String correlationId) {
        return brainGateway.cancel(correlationId);
    }

    public record ChatRequest(
            @NotBlank String message,
            String correlationId,
            String turnMode,
            String parentCorrelationId,
            String clientSessionId) {}
}
