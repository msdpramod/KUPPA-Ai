package ai.kuppa.memory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    private final PersonaMemoryRepository repository;

    public MemoryController(PersonaMemoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PersonaMemory> list() {
        return repository.findByActiveTrueOrderByUpdatedAtDesc();
    }

    @PostMapping
    public PersonaMemory add(@Valid @RequestBody AddMemoryRequest request) {
        boolean explicit = request.source() == null || request.source().isBlank() || "OWNER_EXPLICIT".equalsIgnoreCase(request.source());
        double confidence = request.confidence() == null ? (explicit ? 1.0 : 0.55) : request.confidence();
        boolean reviewed = request.reviewed() == null ? explicit : request.reviewed();
        return repository.save(new PersonaMemory(request.category(), request.content(), confidence, request.source(), reviewed));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<PersonaMemory> review(@PathVariable String id, @RequestBody ReviewMemoryRequest request) {
        return repository.findById(id)
                .map(memory -> {
                    memory.review(request.approved());
                    return ResponseEntity.ok(repository.save(memory));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record AddMemoryRequest(
            @NotBlank String category,
            @NotBlank String content,
            @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
            String source,
            Boolean reviewed) {}

    public record ReviewMemoryRequest(boolean approved) {}
}
