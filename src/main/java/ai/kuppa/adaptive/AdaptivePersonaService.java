package ai.kuppa.adaptive;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AdaptivePersonaService {
    private final ObservedSignalRepository repository;

    public AdaptivePersonaService(ObservedSignalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<ObservedSignal> observeUserMessage(String message) {
        List<ObservedSignal> learned = new ArrayList<>();
        String text = message == null ? "" : message.trim();
        if (text.isEmpty()) return learned;

        if (text.length() < 80) learned.add(save("COMMUNICATION_STYLE", "Prefers concise, direct phrasing", 0.62, text));
        if (text.contains("?") && text.length() < 120) learned.add(save("DECISION_STYLE", "Often asks for a direct recommendation before proceeding", 0.58, text));

        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("sure") || lower.contains("ok") || lower.contains("yes")) {
            learned.add(save("INTERACTION_STYLE", "Uses short confirmation language when agreeing", 0.55, text));
        }
        if (lower.contains("need") || lower.contains("should") || lower.contains("must")) {
            learned.add(save("COMMUNICATION_STYLE", "Expresses priorities in imperative language", 0.57, text));
        }

        String emotion = inferEmotion(lower);
        if (emotion != null) {
            learned.add(save("EMOTIONAL_SIGNAL", emotion, 0.40, text));
        }
        return learned;
    }

    public List<ObservedSignal> activeSignals() {
        return repository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public ObservedSignal confirm(String id) {
        ObservedSignal signal = get(id);
        signal.confirm();
        return repository.save(signal);
    }

    @Transactional
    public ObservedSignal reject(String id) {
        ObservedSignal signal = get(id);
        signal.deactivate();
        return repository.save(signal);
    }

    private ObservedSignal save(String category, String value, double confidence, String source) {
        return repository.save(new ObservedSignal(category, value, confidence, source));
    }

    private String inferEmotion(String lower) {
        if (containsAny(lower, "angry", "frustrated", "annoyed", "irritated")) return "Possible frustration or irritation; ask rather than assume";
        if (containsAny(lower, "happy", "excited", "great", "awesome")) return "Possible positive or excited mood; ask rather than assume";
        if (containsAny(lower, "worried", "anxious", "scared", "nervous")) return "Possible worry or anxiety; ask rather than assume";
        if (containsAny(lower, "sad", "down", "upset")) return "Possible low or upset mood; ask rather than assume";
        return null;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }

    private ObservedSignal get(String id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Signal not found: " + id));
    }
}
