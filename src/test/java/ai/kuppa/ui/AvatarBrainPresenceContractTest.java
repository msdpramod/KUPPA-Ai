package ai.kuppa.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarBrainPresenceContractTest {

    @Test
    void avatarUiConsumesVayuGatewayHealthWithoutAddingBrainLogic() throws IOException {
        String html = resource("/static/index.html");
        assertTrue(html.contains("data-brain=\"unknown\""));
        assertTrue(html.contains("function applyBrainStatus(brain)"));
        assertTrue(html.contains("applyBrainStatus(data.brain)"));
        assertTrue(html.contains("brain.errorCode==='VAYU_UNAVAILABLE'"));
        assertTrue(html.contains("brain.errorCode==='VAYU_CANCELLED'"));
        assertTrue(html.contains("brain.provider==='NONE'"));
        assertTrue(html.contains("brain.degraded"));
        assertTrue(html.contains("brain.latencyMs"));
        assertTrue(html.contains("brain.correlationId"));
        assertTrue(html.contains("brain.contractVersion"));
        assertTrue(html.contains("kuppa-brain-state-change"));
        assertTrue(html.contains("Vayu unavailable"));
        assertTrue(html.contains("Vayu fallback"));
        assertTrue(html.contains("/api/chat"));
        assertTrue(html.contains("PENDING_APPROVAL"));
    }

    @Test
    void avatarUiCancelsAndSupersedesVayuTurnsByCorrelationId() throws IOException {
        String html = resource("/static/index.html");
        assertTrue(html.contains("function newCorrelationId()"));
        assertTrue(html.contains("correlationId:turn.correlationId"));
        assertTrue(html.contains("/cancel`"));
        assertTrue(html.contains("function cancelActiveTurn"));
        assertTrue(html.contains("turn.cancelRequested"));
        assertTrue(html.contains("turn.superseded"));
        assertTrue(html.contains("isCurrentTurn(turn)"));
        assertTrue(html.contains("kuppa-turn-cancelled"));
        assertTrue(html.contains("await cancelActiveTurn('topic-change')"));
        assertTrue(html.contains("await cancelActiveTurn('barge-in')"));
        assertTrue(html.contains("playbackFinish"));
        assertTrue(html.contains("Vayu · turn interrupted"));
    }

    @Test
    void avatarUiUsesExplicitV3ContinuityWithoutInferringSemantics() throws IOException {
        String html = resource("/static/index.html");
        assertTrue(html.contains("AUTO:'AUTO'"));
        assertTrue(html.contains("NEW_TOPIC:'NEW_TOPIC'"));
        assertTrue(html.contains("CONTINUE:'CONTINUE'"));
        assertTrue(html.contains("CORRECTION:'CORRECTION'"));
        assertTrue(html.contains("data-turn-mode=\"CONTINUE\""));
        assertTrue(html.contains("data-turn-mode=\"CORRECTION\""));
        assertTrue(html.contains("data-turn-mode=\"NEW_TOPIC\""));
        assertTrue(html.contains("function selectTurnMode(mode)"));
        assertTrue(html.contains("function consumeTurnContext()"));
        assertTrue(html.contains("lastCompletedCorrelationId"));
        assertTrue(html.contains("turnMode:turn.turnMode"));
        assertTrue(html.contains("parentCorrelationId:turn.parentCorrelationId"));
        assertTrue(html.contains("kuppa-turn-context-change"));
        assertTrue(html.contains("kuppa-turn-completed"));
        assertTrue(html.contains("pendingTurnMode=TURN_MODES.AUTO"));
        assertTrue(html.contains("contractVersion:'v3'"));
    }

    @Test
    void avatarUiRestoresContinuityByOpaqueBrowserSessionWithoutTranscriptRecovery() throws IOException {
        String html = resource("/static/index.html");
        assertTrue(html.contains("kuppa.clientSessionId.v1"));
        assertTrue(html.contains("localStorage.getItem"));
        assertTrue(html.contains("clientSessionId:clientSessionId"));
        assertTrue(html.contains("/api/chat/resumable?clientSessionId="));
        assertTrue(html.contains("function restoreContinuity()"));
        assertTrue(html.contains("kuppa-continuity-restored"));
        assertTrue(html.contains("lastCompletedCorrelationId=data.correlationId"));
    }

    private String resource(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing test resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
