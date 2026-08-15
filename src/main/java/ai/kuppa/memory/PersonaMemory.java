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
    @Column private Double confidence;
    @Column private String source;
    @Column private Boolean reviewed;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected PersonaMemory() {}

    public PersonaMemory(String category, String content) {
        this(category, content, 1.0, "OWNER_EXPLICIT", true);
    }

    public PersonaMemory(String category, String content, Double confidence, String source, Boolean reviewed) {
        this.category = category;
        this.content = content;
        this.confidence = sanitizeConfidence(confidence);
        this.source = source == null || source.isBlank() ? "OWNER_EXPLICIT" : source.trim();
        this.reviewed = reviewed == null ? Boolean.FALSE : reviewed;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public double getConfidence() { return confidence == null ? 1.0 : confidence; }
    public String getSource() { return source == null || source.isBlank() ? "LEGACY" : source; }
    public boolean isReviewed() { return reviewed == null || reviewed; }

    public void review(boolean approved) {
        this.reviewed = true;
        this.active = approved;
    }

    private static double sanitizeConfidence(Double value) {
        if (value == null) return 1.0;
        return Math.max(0.0, Math.min(1.0, value));
    }
}
