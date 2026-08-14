package ai.kuppa.adaptive;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/persona/signals")
public class AdaptivePersonaController {
    private final AdaptivePersonaService service;

    public AdaptivePersonaController(AdaptivePersonaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ObservedSignal> list() { return service.activeSignals(); }

    @PostMapping("/{id}/confirm")
    public ObservedSignal confirm(@PathVariable String id) { return service.confirm(id); }

    @PostMapping("/{id}/reject")
    public ObservedSignal reject(@PathVariable String id) { return service.reject(id); }
}
