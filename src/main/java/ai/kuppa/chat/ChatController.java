package ai.kuppa.chat;

import ai.kuppa.conversation.VayuBrainGateway;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;
    private final VayuBrainGateway brainGateway;
    private final ChatContinuityService continuityService;

    public ChatController(ChatService service, VayuBrainGateway brainGateway, ChatContinuityService continuityService) {
        this.service = service;
        this.brainGateway = brainGateway;
        this.continuityService = continuityService;
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

    @GetMapping("/resumable")
    public ChatContinuityService.ResumableTurn resumable(@RequestParam String clientSessionId) {
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
