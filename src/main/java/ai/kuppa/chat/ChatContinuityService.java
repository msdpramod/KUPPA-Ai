package ai.kuppa.chat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Pattern;

@Service
public class ChatContinuityService {
    private static final Pattern SESSION_ID = Pattern.compile("[A-Za-z0-9._:-]{8,128}");
    private final ChatMessageRepository repository;

    public ChatContinuityService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ResumableTurn latest(String clientSessionId) {
        String session = normalizeSessionId(clientSessionId);
        if (session == null) return ResumableTurn.unavailable();

        return repository
                .findFirstByClientSessionIdAndRoleAndCorrelationIdIsNotNullOrderByCreatedAtDesc(session, "KUPPA_AI")
                .map(message -> new ResumableTurn(true, message.getCorrelationId(), message.getCreatedAt()))
                .orElseGet(ResumableTurn::unavailable);
    }

    static String normalizeSessionId(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return SESSION_ID.matcher(normalized).matches() ? normalized : null;
    }

    public record ResumableTurn(boolean available, String correlationId, Instant completedAt) {
        static ResumableTurn unavailable() {
            return new ResumableTurn(false, null, null);
        }
    }
}
