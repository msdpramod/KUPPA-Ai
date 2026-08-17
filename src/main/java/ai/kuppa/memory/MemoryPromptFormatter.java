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
                    if (isConfirmed(item)) confirmed.append(line);
                    else tentative.append(line);
                });

        StringBuilder out = new StringBuilder();
        out.append("MEMORY FRESHNESS RULE: entries are ordered newest-first by updatedAt. If two memories conflict, prefer the newer reviewed memory; confidence may rank hypotheses but never turns an unreviewed inference into a fact.\n");
        if (!confirmed.isEmpty()) {
            out.append("CONFIRMED MEMORY — owner-provided/reviewed information that may be used as factual persona context:\n")
                    .append(confirmed);
        }
        if (!tentative.isEmpty()) {
            out.append("TENTATIVE MEMORY — inferred and not yet owner-reviewed. Treat confidence only as hypothesis strength; never state these as facts or make external decisions from them without confirmation:\n")
                    .append(tentative);
        }
        return out.toString().trim();
    }

    private boolean isConfirmed(PersonaMemory memory) {
        return memory.isReviewed();
    }

    private Instant updatedAtSafe(PersonaMemory memory) {
        Instant updatedAt = memory.getUpdatedAt();
        return updatedAt == null ? Instant.EPOCH : updatedAt;
    }
}
