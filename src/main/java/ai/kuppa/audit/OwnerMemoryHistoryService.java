package ai.kuppa.audit;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OwnerMemoryHistoryService {
    static final Set<String> MEMORY_EVENT_TYPES = Set.of(
            "MEMORY_CAPTURED",
            "MEMORY_FORGOTTEN",
            "MEMORY_FORGET_NO_MATCH");

    private final AuditEventRepository repository;

    public OwnerMemoryHistoryService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public List<MemoryHistoryEvent> history(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        return repository.findByEventTypeInOrderByCreatedAtDesc(MEMORY_EVENT_TYPES).stream()
                .limit(limit)
                .map(this::toTypedEvent)
                .toList();
    }

    private MemoryHistoryEvent toTypedEvent(AuditEvent event) {
        String category = null;
        Double confidence = null;
        String source = null;
        Integer affectedCount = null;
        List<String> categories = List.of();

        if (event.getDetail() != null) {
            for (String rawPart : event.getDetail().split(",")) {
                String part = rawPart.trim();
                String[] pair = part.split("=", 2);
                if (pair.length != 2) continue;
                String key = pair[0].trim();
                String value = pair[1].trim();
                if ("category".equals(key)) category = bounded(value, 64);
                if ("confidence".equals(key)) confidence = parseConfidence(value);
                if ("source".equals(key)) source = bounded(value, 64);
                if ("affectedCount".equals(key)) affectedCount = parseNonNegativeInt(value);
                if ("categories".equals(key)) categories = parseCategories(value);
            }
        }

        return new MemoryHistoryEvent(
                event.getEventType(),
                category,
                confidence,
                source,
                affectedCount,
                categories,
                event.getCreatedAt());
    }

    private Double parseConfidence(String value) {
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < 0.0 || parsed > 1.0) return null;
            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Integer parseNonNegativeInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed < 0 ? null : parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<String> parseCategories(String value) {
        if (value == null || value.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        for (String raw : value.split("\\|")) {
            String category = bounded(raw.trim().toUpperCase(Locale.ROOT), 64);
            if (category != null && !category.isBlank() && !result.contains(category)) result.add(category);
            if (result.size() == 16) break;
        }
        return List.copyOf(result);
    }

    private String bounded(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    public record MemoryHistoryEvent(
            String eventType,
            String category,
            Double confidence,
            String source,
            Integer affectedCount,
            List<String> categories,
            java.time.Instant createdAt) {}
}
