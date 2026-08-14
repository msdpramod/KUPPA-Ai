package ai.kuppa.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditEventRepository repository;
    public AuditService(AuditEventRepository repository) { this.repository = repository; }
    public void record(String eventType, String actionId, String detail) {
        repository.save(new AuditEvent(eventType, actionId, detail));
    }
}
