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

    public ChatController(ChatService service, VayuBrainGateway brainGateway) {
        this.service = service;
        this.brainGateway = brainGateway;
    }

    @PostMapping
    public ChatService.ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(
                request.message(),
                request.correlationId(),
                request.turnMode(),
                request.parentCorrelationId());
    }

    @PostMapping("/{correlationId}/cancel")
    public VayuBrainGateway.Cancellation cancel(@PathVariable String correlationId) {
        return brainGateway.cancel(correlationId);
    }

    public record ChatRequest(
            @NotBlank String message,
            String correlationId,
            String turnMode,
            String parentCorrelationId) {}
}
