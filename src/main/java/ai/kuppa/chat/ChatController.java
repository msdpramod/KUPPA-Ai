package ai.kuppa.chat;

import ai.kuppa.conversation.VayuBrainGateway;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;
    private final VayuBrainGateway brainGateway;
    private final ChatContinuityService continuityService;
    private final ContinuitySessionService continuitySessionService;
    private final OwnerDeviceIdentityService ownerDeviceIdentityService;
    private final OwnerDeviceTrustService ownerDeviceTrustService;

    public ChatController(ChatService service, VayuBrainGateway brainGateway,
                          ChatContinuityService continuityService,
                          ContinuitySessionService continuitySessionService,
                          OwnerDeviceIdentityService ownerDeviceIdentityService,
                          OwnerDeviceTrustService ownerDeviceTrustService) {
        this.service = service;
        this.brainGateway = brainGateway;
        this.continuityService = continuityService;
        this.continuitySessionService = continuitySessionService;
        this.ownerDeviceIdentityService = ownerDeviceIdentityService;
        this.ownerDeviceTrustService = ownerDeviceTrustService;
    }

    @PostMapping
    public ChatService.ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(
                request.message(),
                request.correlationId(),
                request.turnMode(),
                request.parentCorrelationId(),
                request.clientSessionId());
    }

    @PostMapping("/session")
    public ContinuitySessionService.SessionCredential createContinuitySession() {
        if (!continuitySessionService.enabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Secure continuity sessions require KUPPA_CONTINUITY_SIGNING_SECRET");
        }
        return continuitySessionService.issue();
    }

    @PostMapping("/owner/device")
    public OwnerDeviceIdentityService.DeviceCredential enrollOwnerDevice(
            @RequestHeader(value = "X-KUPPA-Owner-Enroll-Key", required = false) String enrollmentKey,
            @RequestBody(required = false) DeviceEnrollmentRequest request) {
        if (!ownerDeviceIdentityService.enabled()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Owner device enrollment requires KUPPA_OWNER_ENROLLMENT_SECRET");
        }
        try {
            OwnerDeviceIdentityService.DeviceCredential credential = ownerDeviceIdentityService.enroll(
                    enrollmentKey,
                    request == null ? null : request.deviceLabel());
            ownerDeviceTrustService.register(credential);
            return credential;
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner enrollment credential rejected");
        }
    }

    @PostMapping("/owner/device/revoke")
    public DeviceRevocation revokeOwnerDevice(
            @RequestParam String deviceId,
            @RequestHeader(value = "X-KUPPA-Device-Token", required = false) String deviceToken) {
        requireValidDeviceCredential(deviceId, deviceToken);
        boolean revoked = ownerDeviceTrustService.revoke(ownerDeviceIdentityService.ownerId(), deviceId);
        if (!revoked) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner device is not active");
        }
        return new DeviceRevocation(deviceId, true);
    }

    @PostMapping("/session/owner")
    public OwnerContinuityCredential createOwnerContinuitySession(
            @RequestParam String deviceId,
            @RequestHeader(value = "X-KUPPA-Device-Token", required = false) String deviceToken) {
        requireValidDeviceCredential(deviceId, deviceToken);
        if (!ownerDeviceTrustService.authorizeValidatedCredential(
                ownerDeviceIdentityService.ownerId(), deviceId, deviceToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner device is revoked");
        }
        if (!continuitySessionService.enabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Secure continuity sessions are disabled");
        }

        ContinuitySessionService.SessionCredential session = continuitySessionService.issue();
        if (!ownerDeviceTrustService.recordContinuityIssue(ownerDeviceIdentityService.ownerId(), deviceId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Owner device is no longer active");
        }
        return new OwnerContinuityCredential(
                ownerDeviceIdentityService.ownerId(),
                deviceId,
                session.clientSessionId(),
                session.token(),
                session.expiresAt());
    }

    @GetMapping("/resumable")
    public ChatContinuityService.ResumableTurn resumable(@RequestParam String clientSessionId) {
        return continuityService.latest(clientSessionId);
    }

    @GetMapping("/resumable/secure")
    public ChatContinuityService.ResumableTurn secureResumable(
            @RequestParam String clientSessionId,
            @RequestHeader(value = "X-KUPPA-Continuity-Token", required = false) String token) {
        if (!continuitySessionService.enabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Secure continuity sessions are disabled");
        }
        if (!continuitySessionService.validate(clientSessionId, token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired continuity credential");
        }
        return continuityService.latest(clientSessionId);
    }

    @PostMapping("/{correlationId}/cancel")
    public VayuBrainGateway.Cancellation cancel(@PathVariable String correlationId) {
        return brainGateway.cancel(correlationId);
    }

    private void requireValidDeviceCredential(String deviceId, String deviceToken) {
        if (!ownerDeviceIdentityService.enabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Owner device identity is disabled");
        }
        if (!ownerDeviceIdentityService.validate(deviceId, deviceToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired owner device credential");
        }
    }

    public record ChatRequest(
            @NotBlank String message,
            String correlationId,
            String turnMode,
            String parentCorrelationId,
            String clientSessionId) {}

    public record DeviceEnrollmentRequest(String deviceLabel) {}

    public record DeviceRevocation(String deviceId, boolean revoked) {}

    public record OwnerContinuityCredential(
            String ownerId,
            String deviceId,
            String clientSessionId,
            String continuityToken,
            java.time.Instant expiresAt) {}
}
