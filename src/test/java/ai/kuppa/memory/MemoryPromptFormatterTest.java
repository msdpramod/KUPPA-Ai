package ai.kuppa.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryPromptFormatterTest {

    private final MemoryPromptFormatter formatter = new MemoryPromptFormatter();

    @Test
    void separatesConfirmedAndTentativeMemory() {
        PersonaMemory explicit = new PersonaMemory("preference", "Prefers concise answers", 1.0, "OWNER_EXPLICIT", true);
        PersonaMemory inferred = new PersonaMemory("emotion", "May be frustrated by repeated setup failures", 0.45, "INFERRED_FROM_CONVERSATION", false);

        String prompt = formatter.format(List.of(explicit, inferred));

        assertThat(prompt).contains("CONFIRMED OR HIGH-CONFIDENCE MEMORY");
        assertThat(prompt).contains("Prefers concise answers");
        assertThat(prompt).contains("TENTATIVE MEMORY");
        assertThat(prompt).contains("May be frustrated by repeated setup failures");
        assertThat(prompt).contains("never as a fact");
    }

    @Test
    void highConfidenceInferenceCanBeUsedWithoutBeingMarkedTentative() {
        PersonaMemory inferred = new PersonaMemory("routine", "Usually works on KUPPA in the evening", 0.90, "INFERRED_FROM_PATTERN", false);

        String prompt = formatter.format(List.of(inferred));

        assertThat(prompt).contains("CONFIRMED OR HIGH-CONFIDENCE MEMORY");
        assertThat(prompt).doesNotContain("TENTATIVE MEMORY");
    }
}
