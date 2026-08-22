package ai.kuppa.memory;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
public class MemoryPromptFormatter {

    private static final int MAX_MEMORY_ITEMS = 30;
    private static final Duration EMOTIONAL_SIGNAL_TTL = Duration.ofHours(24);

    public String format(List<PersonaMemory> memory) {
        if (memory == null || memory.isEmpty()) return "No persona memory yet.";

        Instant now = Instant.now();
        StringBuilder style = new StringBuilder();
        StringBuilder confirmed = new StringBuilder();
        StringBuilder tentative = new StringBuilder();

        memory.stream()
                .filter(item -> isFreshEnough(item, now))
                .sorted(Comparator.comparing(this::updatedAtSafe).reversed())
                .limit(MAX_MEMORY_ITEMS)
                .forEach(item -> {
                    String line = "- " + item.getCategory() + ": " + item.getContent()
                            + " [source=" + item.getSource()
                            + ", confidence=" + String.format("%.2f", item.getConfidence())
                            + ", updatedAt=" + updatedAtSafe(item) + "]\n";
                    if (isConfirmedCommunicationStyle(item)) style.append(line);
                    else if (isConfirmed(item)) confirmed.append(line);
                    else tentative.append(line);
                });

        if (style.isEmpty() && confirmed.isEmpty() && tentative.isEmpty()) {
            return "No current persona memory yet.";
        }

        StringBuilder out = new StringBuilder();
        out.append("MEMORY FRESHNESS RULE: entries are ordered newest-first by updatedAt. If two memories conflict, prefer the newer reviewed memory; confidence may rank hypotheses but never turns an unreviewed inference into a fact. Emotional signals are short-lived context and expire from prompts after 24 hours.\n");
        if (!style.isEmpty()) {
            out.append("OWNER COMMUNICATION STYLE — explicit reviewed instructions for how KUPPA should communicate. Follow these on every response unless the owner gives a newer conflicting instruction:\n")
                    .append(style);
        }
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

    private boolean isConfirmedCommunicationStyle(PersonaMemory memory) {
        return memory.isReviewed()
                && "COMMUNICATION_STYLE".equalsIgnoreCase(memory.getCategory());
    }

    private boolean isFreshEnough(PersonaMemory memory, Instant now) {
        if (!"EMOTIONAL_SIGNAL".equalsIgnoreCase(memory.getCategory())) return true;
        Instant updatedAt = updatedAtSafe(memory);
        return !updatedAt.equals(Instant.EPOCH)
                && !updatedAt.isBefore(now.minus(EMOTIONAL_SIGNAL_TTL));
    }

    private Instant updatedAtSafe(PersonaMemory memory) {
        Instant updatedAt = memory.getUpdatedAt();
        return updatedAt == null ? Instant.EPOCH : updatedAt;
    }
}
