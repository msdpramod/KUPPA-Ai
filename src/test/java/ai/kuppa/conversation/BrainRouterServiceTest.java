package ai.kuppa.conversation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrainRouterServiceTest {

    @Test
    void reportsOllamaAsHealthyPrimaryBrainProvider() throws Exception {
        OllamaConversationService ollama = mock(OllamaConversationService.class);
        OpenAiConversationService openAi = mock(OpenAiConversationService.class);
        when(ollama.answer("hello", List.of())).thenReturn("hello back");

        BrainRouterService.BrainAnswer answer = new BrainRouterService(ollama, openAi, false)
                .answerDetailed("hello", List.of());

        assertThat(answer.message()).isEqualTo("hello back");
        assertThat(answer.provider()).isEqualTo("OLLAMA");
        assertThat(answer.degraded()).isFalse();
        assertThat(answer.errorCode()).isNull();
    }

    @Test
    void reportsOpenAiFallbackWithoutPretendingOllamaWasHealthy() throws Exception {
        OllamaConversationService ollama = mock(OllamaConversationService.class);
        OpenAiConversationService openAi = mock(OpenAiConversationService.class);
        when(ollama.answer("hello", List.of())).thenThrow(new IllegalStateException("offline"));
        when(openAi.answer("hello", List.of())).thenReturn("fallback answer");

        BrainRouterService.BrainAnswer answer = new BrainRouterService(ollama, openAi, true)
                .answerDetailed("hello", List.of());

        assertThat(answer.message()).isEqualTo("fallback answer");
        assertThat(answer.provider()).isEqualTo("OPENAI_FALLBACK");
        assertThat(answer.degraded()).isTrue();
        assertThat(answer.errorCode()).isEqualTo("OLLAMA_UNAVAILABLE");
    }

    @Test
    void failsPresentlyAndClearlyWhenNoBrainProviderIsAvailable() throws Exception {
        OllamaConversationService ollama = mock(OllamaConversationService.class);
        OpenAiConversationService openAi = mock(OpenAiConversationService.class);
        when(ollama.answer("hello", List.of())).thenThrow(new IllegalStateException("secret backend detail"));

        BrainRouterService.BrainAnswer answer = new BrainRouterService(ollama, openAi, false)
                .answerDetailed("hello", List.of());

        assertThat(answer.message()).contains("Vayu’s reasoning service is temporarily unavailable");
        assertThat(answer.message()).doesNotContain("secret backend detail");
        assertThat(answer.provider()).isEqualTo("NONE");
        assertThat(answer.degraded()).isTrue();
        assertThat(answer.errorCode()).isEqualTo("VAYU_UNAVAILABLE");
    }
}
