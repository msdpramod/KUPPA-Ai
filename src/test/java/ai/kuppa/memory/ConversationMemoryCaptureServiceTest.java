package ai.kuppa.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversationMemoryCaptureServiceTest {
    private PersonaMemoryRepository repository() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
    }

    @Test
    void capturesExplicitPreferenceAsReviewedOwnerMemory() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("I prefer concise technical answers").orElseThrow();
        assertEquals("PREFERENCE", memory.getCategory());
        assertEquals("OWNER_EXPLICIT", memory.getSource());
        assertTrue(memory.isReviewed());
        assertEquals(1.0, memory.getConfidence());
    }

    @Test
    void capturesPersistentCommunicationStyleAsReviewedOwnerMemory() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("Please always answer with concise step-by-step explanations").orElseThrow();
        assertEquals("COMMUNICATION_STYLE", memory.getCategory());
        assertTrue(memory.isReviewed());
    }

    @Test
    void doesNotCaptureOneOffAnswerRequestAsPermanentStyle() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        assertTrue(new ConversationMemoryCaptureService(repository).capture("Answer this question in two sentences").isEmpty());
        verify(repository, never()).save(any(PersonaMemory.class));
    }

    @Test
    void capturesRememberCommandWithoutCommandWords() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("Remember that my preferred backend language is Java").orElseThrow();
        assertEquals("FACT", memory.getCategory());
        assertEquals("my preferred backend language is Java", memory.getContent());
    }

    @Test
    void ignoresOrdinaryConversationAndDuplicateMemory() {
        PersonaMemoryRepository repository = repository();
        PersonaMemory existing = new PersonaMemory("PREFERENCE", "I prefer concise technical answers");
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(existing));
        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        assertTrue(service.capture("Can you explain garbage collection?").isEmpty());
        assertTrue(service.capture("I prefer concise technical answers").isEmpty());
        verify(repository, never()).save(any(PersonaMemory.class));
    }

    @Test
    void capturesExplicitCorrectionAsReviewedMemory() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("Correction: I want Indian English voice responses").orElseThrow();
        assertEquals("CORRECTION", memory.getCategory());
        assertTrue(memory.isReviewed());
    }

    @Test
    void capturesExplicitRoutineAsReviewedMemory() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("Every morning I go to the gym before work").orElseThrow();
        assertEquals("ROUTINE", memory.getCategory());
        assertEquals(1.0, memory.getConfidence());
    }

    @Test
    void capturesEmotionalSelfReportAsTentativeMemory() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        PersonaMemory memory = new ConversationMemoryCaptureService(repository)
                .capture("I'm feeling stressed about tomorrow's interview").orElseThrow();
        assertEquals("EMOTIONAL_SIGNAL", memory.getCategory());
        assertFalse(memory.isReviewed());
        assertEquals(0.65, memory.getConfidence());
    }

    @Test
    void replacesOlderEmotionalSignalWithNewestSelfReport() {
        PersonaMemoryRepository repository = repository();
        PersonaMemory oldEmotion = new PersonaMemory("EMOTIONAL_SIGNAL", "I'm feeling stressed about the interview", 0.65, "OWNER_SELF_REPORT", false);
        PersonaMemory durablePreference = new PersonaMemory("PREFERENCE", "I prefer concise answers");
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(oldEmotion, durablePreference));
        PersonaMemory current = new ConversationMemoryCaptureService(repository).capture("I'm feeling calm now").orElseThrow();
        assertEquals("EMOTIONAL_SIGNAL", current.getCategory());
        assertFalse(oldEmotion.isActive());
        assertTrue(durablePreference.isActive());
    }

    @Test
    void doesNotTreatNeutralSelfDescriptionAsEmotion() {
        PersonaMemoryRepository repository = repository();
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        assertTrue(new ConversationMemoryCaptureService(repository).capture("I am a backend engineer").isEmpty());
        verify(repository, never()).save(any(PersonaMemory.class));
    }

    @Test
    void explicitForgetReturnsPrivacySafeMutationOutcome() {
        PersonaMemoryRepository repository = repository();
        PersonaMemory target = new PersonaMemory("PREFERENCE", "I prefer concise technical answers");
        PersonaMemory unrelated = new PersonaMemory("PREFERENCE", "I prefer dark mode");
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(target, unrelated));

        ConversationMemoryCaptureService.CaptureOutcome outcome = new ConversationMemoryCaptureService(repository)
                .process("Forget that I prefer concise technical answers.");

        assertTrue(outcome.memory().isEmpty());
        assertEquals("FORGOTTEN", outcome.mutation().type());
        assertEquals(1, outcome.mutation().affectedCount());
        assertEquals(List.of("PREFERENCE"), outcome.mutation().categories());
        assertFalse(target.isActive());
        assertTrue(unrelated.isActive());
        verify(repository).save(target);
        verify(repository, never()).save(unrelated);
    }

    @Test
    void nearMatchProducesNoMatchOutcomeWithoutDeletingAnything() {
        PersonaMemoryRepository repository = repository();
        PersonaMemory existing = new PersonaMemory("PREFERENCE", "I prefer concise technical answers");
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(existing));

        ConversationMemoryCaptureService.CaptureOutcome outcome = new ConversationMemoryCaptureService(repository)
                .process("Please forget that I prefer concise answers");

        assertEquals("FORGET_NO_MATCH", outcome.mutation().type());
        assertEquals(0, outcome.mutation().affectedCount());
        assertTrue(existing.isActive());
        verify(repository, never()).save(any(PersonaMemory.class));
    }
}
