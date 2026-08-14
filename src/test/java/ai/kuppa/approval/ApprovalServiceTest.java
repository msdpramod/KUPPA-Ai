package ai.kuppa.approval;

import ai.kuppa.action.*;
import ai.kuppa.audit.AuditService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalServiceTest {
    @Test
    void executionIsBlockedUntilExplicitApproval() {
        ProposedActionRepository repo = mock(ProposedActionRepository.class);
        AuditService audit = mock(AuditService.class);
        ProposedAction action = new ProposedAction("SEND_EMAIL", "Send", "payload", "reason", RiskLevel.MEDIUM);
        when(repo.findById("a1")).thenReturn(Optional.of(action));
        ApprovalService service = new ApprovalService(repo, audit);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.execute("a1"));
        assertTrue(ex.getMessage().contains("explicit approval required"));
        verify(audit).record(eq("EXECUTION_BLOCKED"), eq("a1"), anyString());
    }

    @Test
    void approvedActionCanExecute() {
        ProposedActionRepository repo = mock(ProposedActionRepository.class);
        AuditService audit = mock(AuditService.class);
        ProposedAction action = new ProposedAction("SEND_EMAIL", "Send", "payload", "reason", RiskLevel.MEDIUM);
        when(repo.findById("a1")).thenReturn(Optional.of(action));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        ApprovalService service = new ApprovalService(repo, audit);

        service.approve("a1");
        ProposedAction result = service.execute("a1");
        assertEquals(ActionStatus.EXECUTED, result.getStatus());
    }
}
