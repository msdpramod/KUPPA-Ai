package ai.kuppa.memory;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryPromptFormatterTest {

    private final MemoryPromptFormatter formatter = new MemoryPromptFormatter();

    @Test
    void separatesConfirmedAndTentativeMemory() {
        PersonaMemory explicit = new PersonaMemory("preference", "Prefers concise answers", 1.0, "OWNER_EXPLICIT", true);
        PersonaMemory inferred = new PersonaMemory("emotion", "May be frustrated by repeated setup failures", 0.45, "INFERRED_FROM_CONVERSATION", false);

        String prompt = formatter.format(List.of(explicit, inferred));

        assertThat(prompt).contains("CONFIRMED MEMORY");
        assertThat(prompt).contains("Prefers concise answers");
        assertThat(prompt).contains("TENTATIVE MEMORY");
        assertThat(prompt).contains("May be frustrated by repeated setup failures");
        assertThat(prompt).contains("never state these as facts");
    }

    @Test
    void givesReviewedCommunicationStyleItsOwnHighPrioritySection() {
        PersonaMemory style = new PersonaMemory(
                "COMMUNICATION_STYLE",
                "Please always answer with concise step-by-step explanations",
                1.0,
                "OWNER_EXPLICIT",
                true);
        PersonaMemory preference = new PersonaMemory(
                "PREFERENCE",
                "Prefers Java examples",
                1.0,
                "OWNER_EXPLICIT",
                true);

        String prompt = formatter.format(List.of(preference, style));

        assertThat(prompt).contains("OWNER COMMUNICATION STYLE");
        assertThat(prompt).contains("Follow these on every response unless the owner gives a newer conflicting instruction");
        assertThat(prompt.indexOf("Please always answer with concise step-by-step explanations"))
                .isLessThan(prompt.indexOf("Prefers Java examples"));
    }

    @Test
    void highConfidenceInferenceRemainsTentativeUntilOwnerReviewsIt() {
        PersonaMemory inferred = new PersonaMemory("routine", "Usually works on KUPPA in the evening", 0.90, "INFERRED_FROM_PATTERN", false);

        String prompt = formatter.format(List.of(inferred));

        assertThat(prompt).contains("TENTATIVE MEMORY");
        assertThat(prompt).contains("confidence=0.90");
        assertThat(prompt).doesNotContain("CONFIRMED MEMORY");
        assertThat(prompt).contains("confidence may rank hypotheses but never turns an unreviewed inference into a fact");
    }

    @Test
    void reviewedInferenceCanBecomeConfirmedPersonaContext() {
        PersonaMemory reviewedInference = new PersonaMemory("routine", "Usually works on KUPPA in the evening", 0.90, "INFERRED_FROM_PATTERN", true);

        String prompt = formatter.format(List.of(reviewedInference));

        assertThat(prompt).contains("CONFIRMED MEMORY");
        assertThat(prompt).contains("Usually works on KUPPA in the evening");
        assertThat(prompt).doesNotContain("TENTATIVE MEMORY");
    }

    @Test
    void ordersMemoryByFreshestUpdateEvenWhenCallerPassesOldestFirst() throws Exception {
        PersonaMemory older = new PersonaMemory("preference", "Prefers verbose answers", 1.0, "OWNER_EXPLICIT", true);
        PersonaMemory newer = new PersonaMemory("preference", "Prefers concise answers", 1.0, "OWNER_EXPLICIT", true);
        setUpdatedAt(older, Instant.parse("2026-08-15T08:00:00Z"));
        setUpdatedAt(newer, Instant.parse("2026-08-16T08:00:00Z"));

        String prompt = formatter.format(List.of(older, newer));

        assertThat(prompt.indexOf("Prefers concise answers"))
                .isLessThan(prompt.indexOf("Prefers verbose answers"));
        assertThat(prompt).contains("MEMORY FRESHNESS RULE");
        assertThat(prompt).contains("updatedAt=2026-08-16T08:00:00Z");
    }

    @Test
    void expiresEmotionalSignalsAfterTwentyFourHours() throws Exception {
        PersonaMemory stale = new PersonaMemory("EMOTIONAL_SIGNAL", "I'm stressed about an interview", 0.65, "OWNER_SELF_REPORT", false);
        setUpdatedAt(stale, Instant.now().minusSeconds(25 * 60 * 60));

        String prompt = formatter.format(List.of(stale));

        assertThat(prompt).doesNotContain("I'm stressed about an interview");
        assertThat(prompt).isEqualTo("No current persona memory yet.");
    }

    @Test
    void keepsRecentEmotionalSignalTentative() throws Exception {
        PersonaMemory recent = new PersonaMemory("EMOTIONAL_SIGNAL", "I'm excited about the new role", 0.65, "OWNER_SELF_REPORT", false);
        setUpdatedAt(recent, Instant.now().minusSeconds(60 * 60));

        String prompt = formatter.format(List.of(recent));

        assertThat(prompt).contains("TENTATIVE MEMORY");
        assertThat(prompt).contains("I'm excited about the new role");
        assertThat(prompt).contains("Emotional signals are short-lived context and expire from prompts after 24 hours");
    }

    private static void setUpdatedAt(PersonaMemory memory, Instant value) throws Exception {
        Field field = PersonaMemory.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(memory, value);
    }
}
