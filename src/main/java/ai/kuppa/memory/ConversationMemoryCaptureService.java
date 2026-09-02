package ai.kuppa.memory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ConversationMemoryCaptureService {
    private static final String OWNER_EXPLICIT = "OWNER_EXPLICIT";
    private static final String OWNER_SELF_REPORT = "OWNER_SELF_REPORT";
    private static final String EMOTIONAL_SIGNAL = "EMOTIONAL_SIGNAL";
    private static final double TENTATIVE_EMOTION_CONFIDENCE = 0.65;

    private static final List<String> EMOTIONAL_SIGNALS = List.of(
            "stressed", "anxious", "overwhelmed", "upset", "sad", "low", "happy",
            "excited", "frustrated", "angry", "tired", "exhausted", "lonely", "calm",
            "nervous", "worried"
    );

    private final PersonaMemoryRepository repository;

    public ConversationMemoryCaptureService(PersonaMemoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<PersonaMemory> capture(String message) {
        if (message == null || message.isBlank()) return Optional.empty();

        String raw = message.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        String forgotten = afterPrefix(raw, lower, "forget that ");
        if (forgotten == null) forgotten = afterPrefix(raw, lower, "please forget that ");
        if (forgotten != null) {
            forgetExactActiveMemory(forgotten);
            return Optional.empty();
        }

        Candidate candidate = classify(raw);
        if (candidate == null || candidate.content().isBlank()) return Optional.empty();

        List<PersonaMemory> active = repository.findByActiveTrueOrderByUpdatedAtDesc();
        boolean duplicate = active.stream().anyMatch(existing ->
                existing.getCategory().equalsIgnoreCase(candidate.category())
                        && existing.getContent().equalsIgnoreCase(candidate.content()));
        if (duplicate) return Optional.empty();

        // Emotional state is transient. Keep only the newest self-report active so Ollama
        // does not blend yesterday's stress with today's calm/excitement as simultaneous truth.
        if (EMOTIONAL_SIGNAL.equals(candidate.category())) {
            active.stream()
                    .filter(existing -> EMOTIONAL_SIGNAL.equalsIgnoreCase(existing.getCategory()))
                    .forEach(existing -> existing.review(false));
        }

        PersonaMemory saved = repository.save(new PersonaMemory(
                candidate.category(), candidate.content(), candidate.confidence(),
                candidate.source(), candidate.reviewed()));
        return Optional.of(saved);
    }

    private void forgetExactActiveMemory(String requestedContent) {
        String normalizedRequested = normalizeForExactOwnerMatch(requestedContent);
        if (normalizedRequested.isBlank()) return;

        repository.findByActiveTrueOrderByUpdatedAtDesc().stream()
                .filter(existing -> normalizeForExactOwnerMatch(existing.getContent()).equals(normalizedRequested))
                .forEach(existing -> {
                    existing.review(false);
                    repository.save(existing);
                });
    }

    private String normalizeForExactOwnerMatch(String content) {
        if (content == null) return "";
        String normalized = content.trim().toLowerCase(Locale.ROOT);
        while (!normalized.isEmpty()) {
            char last = normalized.charAt(normalized.length() - 1);
            if (last != '.' && last != '!' && last != '?') break;
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private Candidate classify(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);

        String remembered = afterPrefix(raw, lower, "remember that ");
        if (remembered == null) remembered = afterPrefix(raw, lower, "please remember that ");
        if (remembered != null) return confirmed("FACT", remembered);

        if (isExplicitCommunicationStyle(lower)) {
            return confirmed("COMMUNICATION_STYLE", raw);
        }

        if (lower.startsWith("i prefer ") || lower.startsWith("i don't like ")
                || lower.startsWith("i do not like ") || lower.startsWith("my preference is ")) {
            return confirmed("PREFERENCE", raw);
        }

        String correction = afterPrefix(raw, lower, "correction: ");
        if (correction != null) return confirmed("CORRECTION", correction);

        String fromNowOn = afterPrefix(raw, lower, "from now on, ");
        if (fromNowOn == null) fromNowOn = afterPrefix(raw, lower, "from now on ");
        if (fromNowOn != null) return confirmed("PREFERENCE", "From now on " + fromNowOn);

        if (isExplicitRoutine(lower)) {
            return confirmed("ROUTINE", raw);
        }

        if (isEmotionalSelfReport(lower)) {
            return new Candidate(EMOTIONAL_SIGNAL, raw, TENTATIVE_EMOTION_CONFIDENCE,
                    OWNER_SELF_REPORT, false);
        }

        return null;
    }

    private Candidate confirmed(String category, String content) {
        return new Candidate(category, content, 1.0, OWNER_EXPLICIT, true);
    }

    private boolean isExplicitCommunicationStyle(String lower) {
        return lower.startsWith("always answer ")
                || lower.startsWith("always respond ")
                || lower.startsWith("always reply ")
                || lower.startsWith("please always answer ")
                || lower.startsWith("please always respond ")
                || lower.startsWith("please always reply ")
                || lower.startsWith("i prefer your answers ")
                || lower.startsWith("i prefer your responses ")
                || lower.startsWith("i prefer your replies ")
                || lower.startsWith("when you answer, ")
                || lower.startsWith("when you respond, ")
                || lower.startsWith("when you reply, ");
    }

    private boolean isExplicitRoutine(String lower) {
        return lower.startsWith("i usually ")
                || lower.startsWith("i normally ")
                || lower.startsWith("every morning ")
                || lower.startsWith("every evening ")
                || lower.startsWith("every day ")
                || lower.startsWith("on weekdays i ")
                || lower.startsWith("on weekends i ");
    }

    private boolean isEmotionalSelfReport(String lower) {
        boolean selfReportShape = lower.startsWith("i feel ")
                || lower.startsWith("i'm feeling ")
                || lower.startsWith("i am feeling ")
                || lower.startsWith("i'm ")
                || lower.startsWith("i am ");
        if (!selfReportShape) return false;
        return EMOTIONAL_SIGNALS.stream().anyMatch(lower::contains);
    }

    private String afterPrefix(String raw, String lower, String prefix) {
        if (!lower.startsWith(prefix)) return null;
        return raw.substring(prefix.length()).trim();
    }

    private record Candidate(String category, String content, double confidence,
                             String source, boolean reviewed) {}
}
