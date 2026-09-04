package ai.kuppa.conversation;

import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class VayuBrainGateway {
    public static final String CONTRACT_VERSION = "v3";

    private final BrainRouterService router;
    private final VayuRequestLifecycle lifecycle;

    public VayuBrainGateway(BrainRouterService router, VayuRequestLifecycle lifecycle) {
        this.router = router;
        this.lifecycle = lifecycle;
    }

    public Response ask(String message, List<PersonaMemory> memory) {
        return ask(message, memory, null, TurnContext.auto());
    }

    public Response ask(String message, List<PersonaMemory> memory, String requestedCorrelationId) {
        return ask(message, memory, requestedCorrelationId, TurnContext.auto());
    }

    public Response ask(String message, List<PersonaMemory> memory, String requestedCorrelationId,
                        TurnContext requestedTurnContext) {
        String correlationId = normalizeCorrelationId(requestedCorrelationId);
        TurnContext turnContext = requestedTurnContext == null ? TurnContext.auto() : requestedTurnContext.normalized();
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
                    false,
                    turnContext.mode(),
                    turnContext.parentCorrelationId()
            );
        }

        try {
            BrainRouterService.BrainAnswer answer = router.answerDetailed(message, memory, turnContext);
            long latencyMs = elapsedMs(started);

            if (lifecycle.isCancelled(correlationId)) {
                return cancelledResponse(correlationId, latencyMs, turnContext);
            }

            return new Response(
                    CONTRACT_VERSION,
                    correlationId,
                    answer.message(),
                    answer.provider(),
                    answer.degraded(),
                    latencyMs,
                    answer.errorCode(),
                    false,
                    turnContext.mode(),
                    turnContext.parentCorrelationId()
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

    private Response cancelledResponse(String correlationId, long latencyMs, TurnContext turnContext) {
        return new Response(
                CONTRACT_VERSION,
                correlationId,
                "I stopped that Vayu thought because the turn was interrupted. I’m ready for the new direction.",
                "CANCELLED",
                false,
                latencyMs,
                "VAYU_CANCELLED",
                true,
                turnContext.mode(),
                turnContext.parentCorrelationId()
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

    public record TurnContext(String mode, String parentCorrelationId) {
        private static final Set<String> ALLOWED_MODES = Set.of("AUTO", "NEW_TOPIC", "CONTINUE", "CORRECTION");

        public static TurnContext auto() {
            return new TurnContext("AUTO", null);
        }

        public static TurnContext normalize(String mode, String parentCorrelationId) {
            return new TurnContext(mode, parentCorrelationId).normalized();
        }

        public TurnContext normalized() {
            String normalizedMode = mode == null ? "AUTO" : mode.trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_MODES.contains(normalizedMode)) normalizedMode = "AUTO";
            String normalizedParent = parentCorrelationId == null ? null : parentCorrelationId.trim();
            if (normalizedParent != null && normalizedParent.isBlank()) normalizedParent = null;
            return new TurnContext(normalizedMode, normalizedParent);
        }

        public String reasoningDirective() {
            return switch (mode) {
                case "NEW_TOPIC" -> "Turn continuity: treat this as a new topic. Recent conversation is background only; do not force unresolved references from the previous topic.";
                case "CONTINUE" -> "Turn continuity: continue the prior thought. Resolve references from recent conversation and the indicated parent turn when relevant.";
                case "CORRECTION" -> "Turn continuity: the user is correcting the prior thought. Prefer the current message over conflicting recent conversational context; do not silently preserve the corrected claim.";
                default -> "Turn continuity: infer whether this is a new topic, continuation, or correction from the recent conversation. Do not invent a relationship if the evidence is weak.";
            } + (parentCorrelationId == null ? "" : " Parent correlation ID: " + parentCorrelationId + ".");
        }
    }

    public record Response(
            String contractVersion,
            String correlationId,
            String message,
            String provider,
            boolean degraded,
            long latencyMs,
            String errorCode,
            boolean cancelled,
            String turnMode,
            String parentCorrelationId) {}

    public record Cancellation(
            String contractVersion,
            String correlationId,
            boolean accepted,
            String status) {}
}
