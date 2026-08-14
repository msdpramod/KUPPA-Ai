package ai.kuppa.memory;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "persona_memory")
public class PersonaMemory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String category;
    @Column(nullable = false, length = 4000) private String content;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected PersonaMemory() {}
    public PersonaMemory(String category, String content) {
        this.category = category;
        this.content = content;
    }
    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
