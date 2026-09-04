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

    @Test
    void pairingUsesExplicitContinuityAdapterInsteadOfClassicFunctionGlobals() throws Exception {
        String script = resource("static/trusted-devices.js");
        String adapter = resource("static/kuppa-continuity-adapter.js");
        assertTrue(script.contains("KuppaContinuityAdapter"));
        assertTrue(script.contains("adapter.activateTrustedContinuity()"));
        assertTrue(script.contains("adapter.restoreContinuity()"));
        assertTrue(script.contains("adapter?.forgetDevice()"));
        assertFalse(script.contains("window.issueOwnerContinuity"));
        assertFalse(script.contains("window.restoreContinuity"));
        assertFalse(script.contains("window.forgetOwnerDevice"));
        assertTrue(adapter.contains("version:'v1'"));
        assertTrue(adapter.contains("Object.freeze"));
        assertTrue(adapter.contains("kuppa-continuity-adapter-ready"));
    }

    @Test
    void pairingActivatesSignedContinuityWithoutReloadingThePage() throws Exception {
        String script = resource("static/trusted-devices.js");
        assertTrue(script.contains("kuppa-device-pairing-complete"));
        assertTrue(script.contains("Trusted continuity is active."));
        String pairingFlow = script.substring(script.indexOf("pairForm.addEventListener"), script.indexOf("function currentDeviceId"));
        assertFalse(pairingFlow.contains("location.reload()"));
    }

    @Test
    void trustActivityUsesTypedHistoryAndReusesEphemeralManagementBoundary() throws Exception {
        String script = resource("static/trusted-devices.js");
        assertTrue(script.contains("Trust activity"));
        assertTrue(script.contains("/api/chat/owner/trust-history"));
        assertTrue(script.contains("limit=30"));
        assertTrue(script.contains("kuppa-trust-history-loaded"));
        assertTrue(script.contains("event.eventType"));
        assertTrue(script.contains("event.deviceId"));
        assertTrue(script.contains("event.actor"));
        assertTrue(script.contains("event.reason"));
        assertFalse(script.contains("/api/audit"));
        assertFalse(script.contains("event.detail"));
        assertFalse(script.contains("historyToken"));
        assertFalse(script.contains("localStorage.setItem('kuppa.ownerManagement"));
    }
}
