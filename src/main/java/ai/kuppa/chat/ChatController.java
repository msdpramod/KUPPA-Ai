package ai.kuppa.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;
    public ChatController(ChatService service) { this.service = service; }

    @PostMapping
    public ChatService.ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(request.message());
    }

    public record ChatRequest(@NotBlank String message) {}
}
