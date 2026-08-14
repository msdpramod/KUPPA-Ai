package ai.kuppa.chat;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String role;
    @Column(nullable = false, length = 12000) private String content;
    @Column(nullable = false) private Instant createdAt;

    protected ChatMessage() {}
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.createdAt = Instant.now();
    }
    public String getId() { return id; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
