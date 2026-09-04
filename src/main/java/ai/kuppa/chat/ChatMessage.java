package ai.kuppa.chat;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_correlation_created", columnList = "correlationId,createdAt"),
        @Index(name = "idx_chat_session_role_created", columnList = "clientSessionId,role,createdAt")
})
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String role;
    @Column(nullable = false, length = 12000) private String content;
    @Column private String correlationId;
    @Column private String turnMode;
    @Column private String parentCorrelationId;
    @Column private String clientSessionId;
    @Column(nullable = false) private Instant createdAt;

    protected ChatMessage() {}

    public ChatMessage(String role, String content) {
        this(role, content, null, null, null, null);
    }

    public ChatMessage(String role, String content, String correlationId, String turnMode, String parentCorrelationId) {
        this(role, content, correlationId, turnMode, parentCorrelationId, null);
    }

    public ChatMessage(String role, String content, String correlationId, String turnMode,
                       String parentCorrelationId, String clientSessionId) {
        this.role = role;
        this.content = content;
        this.correlationId = normalize(correlationId);
        this.turnMode = normalize(turnMode);
        this.parentCorrelationId = normalize(parentCorrelationId);
        this.clientSessionId = normalize(clientSessionId);
        this.createdAt = Instant.now();
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public String getId() { return id; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getCorrelationId() { return correlationId; }
    public String getTurnMode() { return turnMode; }
    public String getParentCorrelationId() { return parentCorrelationId; }
    public String getClientSessionId() { return clientSessionId; }
    public Instant getCreatedAt() { return createdAt; }
}
