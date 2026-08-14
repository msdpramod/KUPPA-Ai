package ai.kuppa.memory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonaMemoryRepository extends JpaRepository<PersonaMemory, String> {
    List<PersonaMemory> findByActiveTrueOrderByCreatedAtDesc();
}
