package ai.kuppa.planner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionIntentDetectorTest {
    private final ActionIntentDetector detector = new ActionIntentDetector();

    @Test
    void doesNotTreatTechnicalPostgresDiscussionAsExternalAction() {
        assertEquals(ActionIntentDetector.Intent.NONE,
                detector.detect("Explain PostgreSQL indexing and POST request handling"));
    }

    @Test
    void doesNotTreatEmailArchitectureDiscussionAsAction() {
        assertEquals(ActionIntentDetector.Intent.NONE,
                detector.detect("How should an email service use Kafka retries?"));
    }

    @Test
    void detectsExplicitExternalCommunicationRequests() {
        assertEquals(ActionIntentDetector.Intent.EXTERNAL_COMMUNICATION,
                detector.detect("Can you send this to Rahul?"));
        assertEquals(ActionIntentDetector.Intent.EXTERNAL_COMMUNICATION,
                detector.detect("Please post this on LinkedIn"));
        assertEquals(ActionIntentDetector.Intent.EXTERNAL_COMMUNICATION,
                detector.detect("Reply to that email"));
    }

    @Test
    void detectsExplicitHighImpactRequests() {
        assertEquals(ActionIntentDetector.Intent.HIGH_IMPACT,
                detector.detect("Please transfer 5000 rupees"));
        assertEquals(ActionIntentDetector.Intent.HIGH_IMPACT,
                detector.detect("Can you delete that account?"));
    }
}
