package ai.kuppa.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OwnerManagementAuthServiceTest {
    @Test
    void strongManagementSecretAuthorizesExactMatch() {
        String secret = "0123456789abcdef0123456789abcdef";
        OwnerManagementAuthService service = new OwnerManagementAuthService(secret);

        assertTrue(service.enabled());
        assertTrue(service.authorize(secret));
        assertFalse(service.authorize(secret + "x"));
        assertFalse(service.authorize(null));
    }

    @Test
    void weakOrMissingManagementSecretFailsClosed() {
        assertFalse(new OwnerManagementAuthService("").enabled());
        assertFalse(new OwnerManagementAuthService("too-short").enabled());
        assertFalse(new OwnerManagementAuthService("too-short").authorize("too-short"));
    }
}
