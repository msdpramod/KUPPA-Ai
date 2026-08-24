package ai.kuppa.conversation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VayuBrainGatewayTest {

    @Test
    void exposesVersionedCorrelationAndProviderMetadata() {
        BrainRouterService router = mock(BrainRouterService.class);
        when(router.answerDetailed("hello", List.of()))
                .thenReturn(new BrainRouterService.BrainAnswer("hi", "OLLAMA", false, null));

        VayuBrainGateway.Response response = gateway(router).ask("hello", List.of());

        assertThat(response.contractVersion()).isEqualTo("v2");
        assertThat(response.correlationId()).isNotBlank();
        assertThat(response.message()).isEqualTo("hi");
        assertThat(response.provider()).isEqualTo("OLLAMA");
        assertThat(response.degraded()).isFalse();
        assertThat(response.cancelled()).isFalse();
        assertThat(response.errorCode()).isNull();
        assertThat(response.latencyMs()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void preservesCallerCorrelationIdForObservableCancellation() {
        BrainRouterService router = mock(BrainRouterService.class);
        when(router.answerDetailed("hello", List.of()))
                .thenReturn(new BrainRouterService.BrainAnswer("hi", "OLLAMA", false, null));

        VayuBrainGateway.Response response = gateway(router).ask("hello", List.of(), "turn-123");

        assertThat(response.correlationId()).isEqualTo("turn-123");
    }

    @Test
    void preservesExplicitDegradedBrainState() {
        BrainRouterService router = mock(BrainRouterService.class);
        when(router.answerDetailed("hello", List.of()))
                .thenReturn(new BrainRouterService.BrainAnswer(
                        "Vayu unavailable", "NONE", true, "VAYU_UNAVAILABLE"));

        VayuBrainGateway.Response response = gateway(router).ask("hello", List.of());

        assertThat(response.degraded()).isTrue();
        assertThat(response.provider()).isEqualTo("NONE");
        assertThat(response.errorCode()).isEqualTo("VAYU_UNAVAILABLE");
        assertThat(response.cancelled()).isFalse();
    }

    @Test
    void suppressesBrainResultWhenActiveTurnIsCancelled() throws Exception {
        BrainRouterService router = mock(BrainRouterService.class);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(router.answerDetailed("slow", List.of())).thenAnswer(invocation -> {
            started.countDown();
            release.await(2, TimeUnit.SECONDS);
            return new BrainRouterService.BrainAnswer("stale answer", "OLLAMA", false, null);
        });

        VayuBrainGateway gateway = gateway(router);
        CompletableFuture<VayuBrainGateway.Response> responseFuture = CompletableFuture.supplyAsync(
                () -> gateway.ask("slow", List.of(), "turn-cancel-me"));

        assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
        VayuBrainGateway.Cancellation cancellation = gateway.cancel("turn-cancel-me");
        release.countDown();
        VayuBrainGateway.Response response = responseFuture.get(2, TimeUnit.SECONDS);

        assertThat(cancellation.accepted()).isTrue();
        assertThat(cancellation.status()).isEqualTo("CANCEL_REQUESTED");
        assertThat(response.cancelled()).isTrue();
        assertThat(response.errorCode()).isEqualTo("VAYU_CANCELLED");
        assertThat(response.provider()).isEqualTo("CANCELLED");
        assertThat(response.message()).doesNotContain("stale answer");
    }

    @Test
    void rejectsCancellationForUnknownOrCompletedTurn() {
        VayuBrainGateway gateway = gateway(mock(BrainRouterService.class));

        VayuBrainGateway.Cancellation cancellation = gateway.cancel("not-active");

        assertThat(cancellation.accepted()).isFalse();
        assertThat(cancellation.status()).isEqualTo("NOT_ACTIVE");
    }

    private VayuBrainGateway gateway(BrainRouterService router) {
        return new VayuBrainGateway(router, new VayuRequestLifecycle());
    }
}
