package ai.kuppa.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonaMemoryCorrectionTest {

    @Test
    void supersedeDeactivatesOldMemoryAndKeepsReplacementReference() {
        PersonaMemory oldMemory = new PersonaMemory("preference", "Prefers tea", 0.8, "INFERRED", false);

        oldMemory.supersede("replacement-123");

        assertFalse(oldMemory.isActive());
        assertTrue(oldMemory.isReviewed());
        assertEquals("replacement-123", oldMemory.getSupersededById());
    }

    @Test
    void supersedeRejectsMissingReplacementId() {
        PersonaMemory oldMemory = new PersonaMemory("preference", "Prefers tea");

        assertThrows(IllegalArgumentException.class, () -> oldMemory.supersede("  "));
        assertTrue(oldMemory.isActive());
        assertNull(oldMemory.getSupersededById());
    }
}
