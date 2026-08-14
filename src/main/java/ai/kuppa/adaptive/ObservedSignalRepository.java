package ai.kuppa.adaptive;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObservedSignalRepository extends JpaRepository<ObservedSignal, String> {
    List<ObservedSignal> findByActiveTrueOrderByCreatedAtDesc();
}
