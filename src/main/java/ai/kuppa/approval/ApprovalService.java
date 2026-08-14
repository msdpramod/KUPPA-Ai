package ai.kuppa.approval;

import ai.kuppa.action.*;
import ai.kuppa.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {
    private final ProposedActionRepository repository;
    private final AuditService audit;

    public ApprovalService(ProposedActionRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    @Transactional
    public ProposedAction approve(String id) {
        ProposedAction action = get(id);
        action.approve();
        audit.record("ACTION_APPROVED", id, "Explicit user approval recorded");
        return repository.save(action);
    }

    @Transactional
    public ProposedAction reject(String id) {
        ProposedAction action = get(id);
        action.reject();
        audit.record("ACTION_REJECTED", id, "User rejected proposed action");
        return repository.save(action);
    }

    @Transactional
    public ProposedAction execute(String id) {
        ProposedAction action = get(id);
        if (action.getStatus() != ActionStatus.APPROVED) {
            audit.record("EXECUTION_BLOCKED", id, "Execution denied because approval is missing");
            throw new IllegalStateException("KUPPA AI blocked execution: explicit approval required");
        }
        action.markExecuted("SAFE_MVP_EXECUTOR: action recorded as executed; no external system was contacted.");
        audit.record("ACTION_EXECUTED", id, action.getExecutionResult());
        return repository.save(action);
    }

    private ProposedAction get(String id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Action not found: " + id));
    }
}
