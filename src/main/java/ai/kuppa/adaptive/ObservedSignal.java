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
    @Column(nullable = false) private String source;
    @Column(nullable = false) private boolean confirmed = false;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    protected ObservedSignal() {}

    public ObservedSignal(String category, String value, double confidence, String source) {
        this.category = category;
        this.value = value;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        this.source = source;
    }

    public void confirm() { this.confirmed = true; this.confidence = 1.0; }
    public void deactivate() { this.active = false; }
    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getValue() { return value; }
    public double getConfidence() { return confidence; }
    public String getSource() { return source; }
    public boolean isConfirmed() { return confirmed; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
