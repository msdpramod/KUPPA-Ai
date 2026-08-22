package ai.kuppa.memory;

import jakarta.transaction.Transactional;
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

    @Transactional
    @PostMapping("/{id}/correct")
    public ResponseEntity<PersonaMemory> correct(@PathVariable String id, @Valid @RequestBody CorrectMemoryRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    String category = request.category() == null || request.category().isBlank()
                            ? existing.getCategory()
                            : request.category().trim();
                    PersonaMemory replacement = repository.save(
                            new PersonaMemory(category, request.content(), 1.0, "OWNER_CORRECTION", true));
                    existing.supersede(replacement.getId());
                    repository.save(existing);
                    return ResponseEntity.ok(replacement);
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

    public record CorrectMemoryRequest(
            String category,
            @NotBlank String content) {}
}
