package ai.kuppa.conversation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VayuBrainGatewayTest {

    @Test
    void exposesVersionedCorrelationAndProviderMetadata() {
        BrainRouterService router = mock(BrainRouterService.class);
        when(router.answerDetailed("hello", List.of()))
                .thenReturn(new BrainRouterService.BrainAnswer("hi", "OLLAMA", false, null));

        VayuBrainGateway.Response response = new VayuBrainGateway(router).ask("hello", List.of());

        assertThat(response.contractVersion()).isEqualTo("v1");
        assertThat(response.correlationId()).isNotBlank();
        assertThat(response.message()).isEqualTo("hi");
        assertThat(response.provider()).isEqualTo("OLLAMA");
        assertThat(response.degraded()).isFalse();
        assertThat(response.errorCode()).isNull();
        assertThat(response.latencyMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void preservesExplicitDegradedBrainState() {
        BrainRouterService router = mock(BrainRouterService.class);
        when(router.answerDetailed("hello", List.of()))
                .thenReturn(new BrainRouterService.BrainAnswer(
                        "Vayu unavailable", "NONE", true, "VAYU_UNAVAILABLE"));

        VayuBrainGateway.Response response = new VayuBrainGateway(router).ask("hello", List.of());

        assertThat(response.degraded()).isTrue();
        assertThat(response.provider()).isEqualTo("NONE");
        assertThat(response.errorCode()).isEqualTo("VAYU_UNAVAILABLE");
    }
}
