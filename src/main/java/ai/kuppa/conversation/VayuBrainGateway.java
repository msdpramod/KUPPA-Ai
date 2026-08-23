package ai.kuppa.conversation;

import ai.kuppa.memory.PersonaMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VayuBrainGateway {
    public static final String CONTRACT_VERSION = "v1";

    private final BrainRouterService router;

    public VayuBrainGateway(BrainRouterService router) {
        this.router = router;
    }

    public Response ask(String message, List<PersonaMemory> memory) {
        String correlationId = UUID.randomUUID().toString();
        long started = System.nanoTime();
        BrainRouterService.BrainAnswer answer = router.answerDetailed(message, memory);
        long latencyMs = Math.max(0L, (System.nanoTime() - started) / 1_000_000L);

        return new Response(
                CONTRACT_VERSION,
                correlationId,
                answer.message(),
                answer.provider(),
                answer.degraded(),
                latencyMs,
                answer.errorCode()
        );
    }

    public record Response(
            String contractVersion,
            String correlationId,
            String message,
            String provider,
            boolean degraded,
            long latencyMs,
            String errorCode) {}
}
