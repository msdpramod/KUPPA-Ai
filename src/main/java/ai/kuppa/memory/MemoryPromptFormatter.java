package ai.kuppa.memory;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
public class MemoryPromptFormatter {

    private static final int MAX_MEMORY_ITEMS = 30;

    public String format(List<PersonaMemory> memory) {
        if (memory == null || memory.isEmpty()) return "No persona memory yet.";

        StringBuilder confirmed = new StringBuilder();
        StringBuilder tentative = new StringBuilder();

        memory.stream()
                .sorted(Comparator.comparing(this::updatedAtSafe).reversed())
                .limit(MAX_MEMORY_ITEMS)
                .forEach(item -> {
                    String line = "- " + item.getCategory() + ": " + item.getContent()
                            + " [source=" + item.getSource()
                            + ", confidence=" + String.format("%.2f", item.getConfidence())
                            + ", updatedAt=" + updatedAtSafe(item) + "]\n";
                    if (item.isReviewed() || item.getConfidence() >= 0.80) confirmed.append(line);
                    else tentative.append(line);
                });

        StringBuilder out = new StringBuilder();
        out.append("MEMORY FRESHNESS RULE: entries are ordered newest-first by updatedAt. If two memories conflict, prefer the newer reviewed or higher-confidence memory and do not present both as simultaneously certain.\n");
        if (!confirmed.isEmpty()) {
            out.append("CONFIRMED OR HIGH-CONFIDENCE MEMORY:\n").append(confirmed);
        }
        if (!tentative.isEmpty()) {
            out.append("TENTATIVE MEMORY — treat as a hypothesis, never as a fact; avoid stating it back as certain unless the user confirms it:\n")
                    .append(tentative);
        }
        return out.toString().trim();
    }

    private Instant updatedAtSafe(PersonaMemory memory) {
        Instant updatedAt = memory.getUpdatedAt();
        return updatedAt == null ? Instant.EPOCH : updatedAt;
    }
}
