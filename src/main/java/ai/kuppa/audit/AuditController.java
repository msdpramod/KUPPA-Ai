package ai.kuppa.audit;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditEventRepository repository;
    public AuditController(AuditEventRepository repository) { this.repository = repository; }
    @GetMapping public List<AuditEvent> list() { return repository.findAllByOrderByCreatedAtDesc(); }
}
