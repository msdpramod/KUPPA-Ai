package ai.kuppa.audit;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class OwnerTrustHistoryService {
    static final Set<String> TRUST_EVENT_TYPES = Set.of(
            "OWNER_DEVICE_ENROLLED",
            "OWNER_DEVICE_MIGRATED",
            "OWNER_DEVICE_CONTINUITY_ISSUED",
            "OWNER_DEVICE_REVOKED_SELF",
            "OWNER_DEVICE_REVOKED_REMOTE");

    private final AuditEventRepository repository;

    public OwnerTrustHistoryService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public List<TrustHistoryEvent> history(String deviceId, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        List<AuditEvent> events = (deviceId == null || deviceId.isBlank())
                ? repository.findByEventTypeInOrderByCreatedAtDesc(TRUST_EVENT_TYPES)
                : repository.findByEventTypeInAndActionIdOrderByCreatedAtDesc(TRUST_EVENT_TYPES, deviceId.trim());
        return events.stream().limit(limit).map(this::toTypedEvent).toList();
    }

    private TrustHistoryEvent toTypedEvent(AuditEvent event) {
        String actor = null;
        String reason = null;
        if (event.getDetail() != null) {
            for (String part : event.getDetail().split(";")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2) continue;
                if ("actor".equals(pair[0])) actor = pair[1];
                if ("reason".equals(pair[0])) reason = pair[1];
            }
        }
        return new TrustHistoryEvent(
                event.getEventType(),
                event.getActionId(),
                actor,
                reason,
                event.getCreatedAt());
    }

    public record TrustHistoryEvent(
            String eventType,
            String deviceId,
            String actor,
            String reason,
            java.time.Instant createdAt) {}
}
