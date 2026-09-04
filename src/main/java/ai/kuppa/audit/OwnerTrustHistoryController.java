package ai.kuppa.audit;

import ai.kuppa.chat.OwnerManagementAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/chat/owner")
public class OwnerTrustHistoryController {
    private final OwnerTrustHistoryService trustHistoryService;
    private final OwnerManagementAuthService ownerManagementAuthService;

    public OwnerTrustHistoryController(OwnerTrustHistoryService trustHistoryService,
                                       OwnerManagementAuthService ownerManagementAuthService) {
        this.trustHistoryService = trustHistoryService;
        this.ownerManagementAuthService = ownerManagementAuthService;
    }

    @GetMapping("/trust-history")
    public List<OwnerTrustHistoryService.TrustHistoryEvent> trustHistory(
            @RequestHeader(value = "X-KUPPA-Owner-Management-Key", required = false) String managementKey,
            @RequestParam(required = false) String deviceId,
            @RequestParam(defaultValue = "50") int limit) {
        if (!ownerManagementAuthService.enabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Owner trust history requires KUPPA_OWNER_MANAGEMENT_SECRET");
        }
        if (!ownerManagementAuthService.authorize(managementKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner management credential rejected");
        }
        return trustHistoryService.history(deviceId, limit);
    }
}
