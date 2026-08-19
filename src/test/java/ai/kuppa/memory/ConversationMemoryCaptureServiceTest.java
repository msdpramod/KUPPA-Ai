package ai.kuppa.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversationMemoryCaptureServiceTest {

    @Test
    void capturesExplicitPreferenceAsReviewedOwnerMemory() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        PersonaMemory memory = service.capture("I prefer concise technical answers").orElseThrow();

        assertEquals("PREFERENCE", memory.getCategory());
        assertEquals("I prefer concise technical answers", memory.getContent());
        assertEquals("OWNER_EXPLICIT", memory.getSource());
        assertTrue(memory.isReviewed());
        assertEquals(1.0, memory.getConfidence());
        verify(repository).save(any(PersonaMemory.class));
    }

    @Test
    void capturesRememberCommandWithoutCommandWords() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        PersonaMemory memory = service.capture("Remember that my preferred backend language is Java").orElseThrow();

        assertEquals("FACT", memory.getCategory());
        assertEquals("my preferred backend language is Java", memory.getContent());
    }

    @Test
    void ignoresOrdinaryConversationAndDuplicateMemory() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        PersonaMemory existing = new PersonaMemory("PREFERENCE", "I prefer concise technical answers");
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(existing));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);

        assertTrue(service.capture("Can you explain garbage collection?").isEmpty());
        assertTrue(service.capture("I prefer concise technical answers").isEmpty());
        verify(repository, never()).save(any(PersonaMemory.class));
    }

    @Test
    void capturesExplicitCorrectionAsReviewedMemory() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        PersonaMemory memory = service.capture("Correction: I want Indian English voice responses").orElseThrow();

        assertEquals("CORRECTION", memory.getCategory());
        assertEquals("I want Indian English voice responses", memory.getContent());
        assertTrue(memory.isReviewed());
    }

    @Test
    void capturesExplicitRoutineAsReviewedMemory() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        PersonaMemory memory = service.capture("Every morning I go to the gym before work").orElseThrow();

        assertEquals("ROUTINE", memory.getCategory());
        assertEquals("OWNER_EXPLICIT", memory.getSource());
        assertTrue(memory.isReviewed());
        assertEquals(1.0, memory.getConfidence());
    }

    @Test
    void capturesEmotionalSelfReportAsTentativeMemory() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(repository.save(any(PersonaMemory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);
        PersonaMemory memory = service.capture("I'm feeling stressed about tomorrow's interview").orElseThrow();

        assertEquals("EMOTIONAL_SIGNAL", memory.getCategory());
        assertEquals("OWNER_SELF_REPORT", memory.getSource());
        assertFalse(memory.isReviewed());
        assertEquals(0.65, memory.getConfidence());
    }

    @Test
    void doesNotTreatNeutralSelfDescriptionAsEmotion() {
        PersonaMemoryRepository repository = mock(PersonaMemoryRepository.class);
        when(repository.findByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of());

        ConversationMemoryCaptureService service = new ConversationMemoryCaptureService(repository);

        assertTrue(service.capture("I am a backend engineer").isEmpty());
        verify(repository, never()).save(any(PersonaMemory.class));
    }
}
