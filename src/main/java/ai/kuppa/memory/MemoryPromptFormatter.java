package ai.kuppa.memory;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemoryPromptFormatter {

    public String format(List<PersonaMemory> memory) {
        if (memory == null || memory.isEmpty()) return "No persona memory yet.";

        StringBuilder confirmed = new StringBuilder();
        StringBuilder tentative = new StringBuilder();

        memory.stream().limit(30).forEach(item -> {
            String line = "- " + item.getCategory() + ": " + item.getContent()
                    + " [source=" + item.getSource() + ", confidence=" + String.format("%.2f", item.getConfidence()) + "]\n";
            if (item.isReviewed() || item.getConfidence() >= 0.80) confirmed.append(line);
            else tentative.append(line);
        });

        StringBuilder out = new StringBuilder();
        if (!confirmed.isEmpty()) {
            out.append("CONFIRMED OR HIGH-CONFIDENCE MEMORY:\n").append(confirmed);
        }
        if (!tentative.isEmpty()) {
            out.append("TENTATIVE MEMORY — treat as a hypothesis, never as a fact; avoid stating it back as certain unless the user confirms it:\n")
                    .append(tentative);
        }
        return out.toString().trim();
    }
}
