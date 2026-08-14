package ai.kuppa.adaptive;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ObservedSignalRepository extends JpaRepository<ObservedSignal, String> {
    List<ObservedSignal> findByActiveTrueOrderByLastSeenAtDesc();
    Optional<ObservedSignal> findFirstByCategoryAndValueAndActiveTrue(String category, String value);
}
