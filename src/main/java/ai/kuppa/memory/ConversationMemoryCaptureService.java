package ai.kuppa.memory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ConversationMemoryCaptureService {
    private static final String OWNER_EXPLICIT = "OWNER_EXPLICIT";
    private final PersonaMemoryRepository repository;

    public ConversationMemoryCaptureService(PersonaMemoryRepository repository) {
        this.repository = repository;
    }

    public Optional<PersonaMemory> capture(String message) {
        if (message == null || message.isBlank()) return Optional.empty();

        String raw = message.trim();
        Candidate candidate = classify(raw);
        if (candidate == null || candidate.content().isBlank()) return Optional.empty();

        List<PersonaMemory> active = repository.findByActiveTrueOrderByUpdatedAtDesc();
        boolean duplicate = active.stream().anyMatch(existing ->
                existing.getCategory().equalsIgnoreCase(candidate.category())
                        && existing.getContent().equalsIgnoreCase(candidate.content()));
        if (duplicate) return Optional.empty();

        PersonaMemory saved = repository.save(new PersonaMemory(
                candidate.category(), candidate.content(), 1.0, OWNER_EXPLICIT, true));
        return Optional.of(saved);
    }

    private Candidate classify(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);

        String remembered = afterPrefix(raw, lower, "remember that ");
        if (remembered == null) remembered = afterPrefix(raw, lower, "please remember that ");
        if (remembered != null) return new Candidate("FACT", remembered);

        if (lower.startsWith("i prefer ") || lower.startsWith("i don't like ")
                || lower.startsWith("i do not like ") || lower.startsWith("my preference is ")) {
            return new Candidate("PREFERENCE", raw);
        }

        String correction = afterPrefix(raw, lower, "correction: ");
        if (correction != null) return new Candidate("CORRECTION", correction);

        String fromNowOn = afterPrefix(raw, lower, "from now on, ");
        if (fromNowOn == null) fromNowOn = afterPrefix(raw, lower, "from now on ");
        if (fromNowOn != null) return new Candidate("PREFERENCE", "From now on " + fromNowOn);

        return null;
    }

    private String afterPrefix(String raw, String lower, String prefix) {
        if (!lower.startsWith(prefix)) return null;
        return raw.substring(prefix.length()).trim();
    }

    private record Candidate(String category, String content) {}
}
