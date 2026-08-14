package ai.kuppa.memory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    private final PersonaMemoryRepository repository;
    public MemoryController(PersonaMemoryRepository repository) { this.repository = repository; }

    @GetMapping public List<PersonaMemory> list() { return repository.findByActiveTrueOrderByCreatedAtDesc(); }

    @PostMapping
    public PersonaMemory add(@Valid @RequestBody AddMemoryRequest request) {
        return repository.save(new PersonaMemory(request.category(), request.content()));
    }

    public record AddMemoryRequest(@NotBlank String category, @NotBlank String content) {}
}
