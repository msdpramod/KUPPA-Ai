package ai.kuppa.action;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProposedActionRepository extends JpaRepository<ProposedAction, String> {
    List<ProposedAction> findAllByOrderByCreatedAtDesc();
}
