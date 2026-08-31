package ai.kuppa.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TrustedDevicesUiContractTest {
    private String resource(String path) throws IOException {
        try (var in = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path + " must exist");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void trustedDevicesSheetUsesMetadataOnlyOwnerManagementApis() throws Exception {
        String script = resource("static/trusted-devices.js");
        assertTrue(script.contains("/api/chat/owner/devices"));
        assertTrue(script.contains("X-KUPPA-Owner-Management-Key"));
        assertTrue(script.contains("Forget on this browser"));
        assertTrue(script.contains("Revoke everywhere"));
        assertTrue(script.contains("managementKey=null"));
        assertFalse(script.contains("ownerManagementKey.v1"));
        assertFalse(script.contains("device.deviceToken"));
        assertFalse(script.contains("device.continuityToken"));
        assertFalse(script.contains("device.enrollmentSecret"));
        assertFalse(script.contains("device.managementSecret"));
        assertFalse(script.contains("device.signingSecret"));
    }

    @Test
    void pairingFlowAvoidsCredentialPromptsAndDoesNotPersistOwnerSecrets() throws Exception {
        String script = resource("static/trusted-devices.js");
        assertTrue(script.contains("Pair this device"));
        assertTrue(script.contains("Pair securely"));
        assertTrue(script.contains("/api/chat/owner/device"));
        assertTrue(script.contains("X-KUPPA-Owner-Enroll-Key"));
        assertTrue(script.contains("autocomplete=\"new-password\""));
        assertTrue(script.contains("pairKey.value=''"));
        assertTrue(script.contains("managementInput.value=''"));
        assertTrue(script.contains("kuppa-device-paired"));
        assertFalse(script.contains("window.prompt"));
        assertFalse(script.contains("kuppa.ownerEnrollmentKey"));
        assertFalse(script.contains("kuppa.ownerManagementKey"));
    }
}
