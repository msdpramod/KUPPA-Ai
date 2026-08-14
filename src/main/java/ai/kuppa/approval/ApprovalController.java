package ai.kuppa.approval;

import ai.kuppa.action.ProposedAction;
import ai.kuppa.action.ProposedActionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/actions")
public class ApprovalController {
    private final ApprovalService service;
    private final ProposedActionRepository repository;

    public ApprovalController(ApprovalService service, ProposedActionRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping public List<ProposedAction> list() { return repository.findAllByOrderByCreatedAtDesc(); }
    @PostMapping("/{id}/approve") public ProposedAction approve(@PathVariable String id) { return service.approve(id); }
    @PostMapping("/{id}/reject") public ProposedAction reject(@PathVariable String id) { return service.reject(id); }
    @PostMapping("/{id}/execute") public ProposedAction execute(@PathVariable String id) { return service.execute(id); }
}
