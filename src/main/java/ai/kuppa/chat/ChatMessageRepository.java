package ai.kuppa.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findTop50ByOrderByCreatedAtDesc();
    List<ChatMessage> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
}
