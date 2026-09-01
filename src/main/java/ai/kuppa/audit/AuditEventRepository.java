package ai.kuppa.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findAllByOrderByCreatedAtDesc();
    List<AuditEvent> findByEventTypeInOrderByCreatedAtDesc(Collection<String> eventTypes);
    List<AuditEvent> findByEventTypeInAndActionIdOrderByCreatedAtDesc(Collection<String> eventTypes, String actionId);
}
