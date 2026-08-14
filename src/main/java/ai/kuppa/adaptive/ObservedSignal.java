package ai.kuppa.adaptive;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "observed_signals")
public class ObservedSignal {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false) private String category;
    @Column(nullable = false, length = 4000) private String value;
    @Column(nullable = false) private double confidence;
    @Column(nullable = false, length = 4000) private String source;
    @Column(nullable = false) private boolean confirmed = false;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private int occurrences = 1;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant lastSeenAt = Instant.now();

    protected ObservedSignal() {}

    public ObservedSignal(String category, String value, double confidence, String source) {
        this.category = category;
        this.value = value;
        this.confidence = clamp(confidence);
        this.source = source;
    }

    public void observeAgain(double newConfidence, String newSource) {
        occurrences++;
        confidence = confirmed ? 1.0 : clamp(Math.max(confidence, newConfidence) + 0.03);
        source = newSource;
        lastSeenAt = Instant.now();
    }

    public void confirm() { confirmed = true; confidence = 1.0; lastSeenAt = Instant.now(); }
    public void deactivate() { active = false; lastSeenAt = Instant.now(); }

    private double clamp(double value) { return Math.max(0.0, Math.min(1.0, value)); }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getValue() { return value; }
    public double getConfidence() { return confidence; }
    public String getSource() { return source; }
    public boolean isConfirmed() { return confirmed; }
    public boolean isActive() { return active; }
    public int getOccurrences() { return occurrences; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
}
