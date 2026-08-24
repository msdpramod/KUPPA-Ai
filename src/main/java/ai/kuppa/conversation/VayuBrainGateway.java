package ai.kuppa.conversation;

import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VayuBrainGateway {
    public static final String CONTRACT_VERSION = "v2";

    private final BrainRouterService router;
    private final VayuRequestLifecycle lifecycle;

    public VayuBrainGateway(BrainRouterService router, VayuRequestLifecycle lifecycle) {
        this.router = router;
        this.lifecycle = lifecycle;
    }

    public Response ask(String message, List<PersonaMemory> memory) {
        return ask(message, memory, null);
    }

    public Response ask(String message, List<PersonaMemory> memory, String requestedCorrelationId) {
        String correlationId = normalizeCorrelationId(requestedCorrelationId);
        long started = System.nanoTime();

        if (!lifecycle.register(correlationId)) {
            return new Response(
                    CONTRACT_VERSION,
                    correlationId,
                    "I couldn’t start that Vayu thought because the request identifier is already active. Please retry with a new request.",
                    "NONE",
                    true,
                    0L,
                    "VAYU_REQUEST_CONFLICT",
                    false
            );
        }

        try {
            BrainRouterService.BrainAnswer answer = router.answerDetailed(message, memory);
            long latencyMs = elapsedMs(started);

            if (lifecycle.isCancelled(correlationId)) {
                return cancelledResponse(correlationId, latencyMs);
            }

            return new Response(
                    CONTRACT_VERSION,
                    correlationId,
                    answer.message(),
                    answer.provider(),
                    answer.degraded(),
                    latencyMs,
                    answer.errorCode(),
                    false
            );
        } finally {
            lifecycle.release(correlationId);
        }
    }

    public Cancellation cancel(String correlationId) {
        String normalized = correlationId == null ? "" : correlationId.trim();
        boolean accepted = !normalized.isBlank() && lifecycle.cancel(normalized);
        return new Cancellation(
                CONTRACT_VERSION,
                normalized,
                accepted,
                accepted ? "CANCEL_REQUESTED" : "NOT_ACTIVE"
        );
    }

    private Response cancelledResponse(String correlationId, long latencyMs) {
        return new Response(
                CONTRACT_VERSION,
                correlationId,
                "I stopped that Vayu thought because the turn was interrupted. I’m ready for the new direction.",
                "CANCELLED",
                false,
                latencyMs,
                "VAYU_CANCELLED",
                true
        );
    }

    private String normalizeCorrelationId(String requestedCorrelationId) {
        if (requestedCorrelationId == null || requestedCorrelationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestedCorrelationId.trim();
    }

    private long elapsedMs(long started) {
        return Math.max(0L, (System.nanoTime() - started) / 1_000_000L);
    }

    public record Response(
            String contractVersion,
            String correlationId,
            String message,
            String provider,
            boolean degraded,
            long latencyMs,
            String errorCode,
            boolean cancelled) {}

    public record Cancellation(
            String contractVersion,
            String correlationId,
            boolean accepted,
            String status) {}
}
