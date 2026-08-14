package ai.kuppa.adaptive;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdaptivePersonaServiceTest {

    @Test
    void learnsCommunicationStyleFromShortDirectMessage() {
        ObservedSignalRepository repository = mock(ObservedSignalRepository.class);
        when(repository.findFirstByCategoryAndValueAndActiveTrue(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(ObservedSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdaptivePersonaService service = new AdaptivePersonaService(repository);
        var learned = service.observeUserMessage("need this fixed");

        assertFalse(learned.isEmpty());
        assertTrue(learned.stream().anyMatch(signal -> "COMMUNICATION_STYLE".equals(signal.getCategory())));
    }

    @Test
    void repeatedObservationStrengthensExistingSignalInsteadOfCreatingDuplicate() {
        ObservedSignalRepository repository = mock(ObservedSignalRepository.class);
        ObservedSignal existing = new ObservedSignal(
                "COMMUNICATION_STYLE",
                "Prefers concise, direct phrasing",
                0.62,
                "first message");

        when(repository.findFirstByCategoryAndValueAndActiveTrue(
                "COMMUNICATION_STYLE", "Prefers concise, direct phrasing"))
                .thenReturn(Optional.of(existing));
        when(repository.findFirstByCategoryAndValueAndActiveTrue(
                "COMMUNICATION_STYLE", "Expresses priorities in imperative language"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ObservedSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdaptivePersonaService service = new AdaptivePersonaService(repository);
        service.observeUserMessage("need it now");

        assertEquals(2, existing.getOccurrences());
        assertTrue(existing.getConfidence() > 0.62);
        verify(repository, never()).save(argThat(signal ->
                signal != existing && "Prefers concise, direct phrasing".equals(signal.getValue())));
    }

    @Test
    void emotionalSignalsRemainLowConfidenceUntilUserConfirmsThem() {
        ObservedSignalRepository repository = mock(ObservedSignalRepository.class);
        when(repository.findFirstByCategoryAndValueAndActiveTrue(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(ObservedSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdaptivePersonaService service = new AdaptivePersonaService(repository);
        var learned = service.observeUserMessage("I am frustrated with this issue");

        ObservedSignal emotional = learned.stream()
                .filter(signal -> "EMOTIONAL_SIGNAL".equals(signal.getCategory()))
                .findFirst()
                .orElseThrow();

        assertEquals(0.40, emotional.getConfidence(), 0.001);
        assertFalse(emotional.isConfirmed());
    }
}
