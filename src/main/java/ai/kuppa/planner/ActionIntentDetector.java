package ai.kuppa.planner;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ActionIntentDetector {
    public enum Intent { NONE, EXTERNAL_COMMUNICATION, HIGH_IMPACT }

    private static final Pattern COMMAND_PREFIX = Pattern.compile(
            "^(please\\s+)?(?:(?:can|could|would|will)\\s+you\\s+|i\\s+want\\s+you\\s+to\\s+|go\\s+ahead\\s+and\\s+)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EXTERNAL_ACTION = Pattern.compile(
            "^(send|reply|publish|dm|message)\\b|^post\\s+(this|that|it|on|to|my|a|an)\\b|^email\\s+(this|that|it|him|her|them|to|my|a|an)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern HIGH_IMPACT_ACTION = Pattern.compile(
            "^(delete|pay|purchase|buy|transfer|withdraw|refund|cancel)\\b",
            Pattern.CASE_INSENSITIVE);

    public Intent detect(String message) {
        if (message == null || message.isBlank()) return Intent.NONE;

        String normalized = message.trim().toLowerCase(Locale.ROOT);
        String commandBody = COMMAND_PREFIX.matcher(normalized).replaceFirst("").trim();

        if (HIGH_IMPACT_ACTION.matcher(commandBody).find()) {
            return Intent.HIGH_IMPACT;
        }
        if (EXTERNAL_ACTION.matcher(commandBody).find()) {
            return Intent.EXTERNAL_COMMUNICATION;
        }
        return Intent.NONE;
    }
}
