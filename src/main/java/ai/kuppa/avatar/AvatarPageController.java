package ai.kuppa.avatar;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class AvatarPageController {
    private static final String TRUSTED_DEVICES_CSS = "<link rel=\"stylesheet\" href=\"/trusted-devices.css\"/>";
    private static final String PRESENCE_CSS = "<link rel=\"stylesheet\" href=\"/kuppa-presence.css\"/>";
    private static final String AVATAR_MOTION_JS = "<script src=\"/kuppa-avatar-motion.js\"></script>";
    private static final String CONTINUITY_ADAPTER_JS = "<script src=\"/kuppa-continuity-adapter.js\"></script>";
    private static final String PRESENCE_JS = "<script src=\"/kuppa-presence.js\"></script>";
    private static final String TRUSTED_DEVICES_JS = "<script src=\"/trusted-devices.js\"></script>";

    private static final String ANIMATION_HEAD = "function animate(){if(!renderer)return;const t=clock.getElapsedTime();lookX+=(targetLookX-lookX)*.06;lookY+=(targetLookY-lookY)*.06;if(avatarModel){";
    private static final String ANIMATION_HEAD_WITH_POLICY = "function animate(){if(!renderer)return;const t=clock.getElapsedTime();const motion=globalThis.KuppaAvatarMotionPolicy;const reduced=!!motion?.isReduced?.();const gazeScale=motion?.gazeScale?.()??1;const motionScale=motion?.autonomousScale?.(state)??1;lookX+=((targetLookX*gazeScale)-lookX)*(reduced?.16:.06);lookY+=((targetLookY*gazeScale)-lookY)*(reduced?.16:.06);if(avatarModel){";
    private static final String YAW_MOTION = "avatarModel.rotation.y=lookX+(state===STATES.THINKING?.035:0)+Math.sin(t*.45)*.006;";
    private static final String YAW_MOTION_WITH_POLICY = "avatarModel.rotation.y=lookX+(state===STATES.THINKING?.035*motionScale:0)+Math.sin(t*.45)*.006*motionScale;";
    private static final String BOB_MOTION = "avatarModel.position.y=baseY+Math.sin(t*1.15)*.008;";
    private static final String BOB_MOTION_WITH_POLICY = "avatarModel.position.y=baseY+Math.sin(t*1.15)*.008*motionScale;";

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String avatar() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html;
        try (var input = resource.getInputStream()) {
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        html = applyAvatarMotionPolicy(html);
        return html.replace("</head>", TRUSTED_DEVICES_CSS + PRESENCE_CSS + AVATAR_MOTION_JS + "</head>")
                .replace("</body>", CONTINUITY_ADAPTER_JS + PRESENCE_JS + TRUSTED_DEVICES_JS + "</body>");
    }

    private String applyAvatarMotionPolicy(String html) throws IOException {
        if (!html.contains(ANIMATION_HEAD) || !html.contains(YAW_MOTION) || !html.contains(BOB_MOTION)) {
            throw new IOException("Avatar motion policy patch points no longer match index.html");
        }
        return html.replace(ANIMATION_HEAD, ANIMATION_HEAD_WITH_POLICY)
                .replace(YAW_MOTION, YAW_MOTION_WITH_POLICY)
                .replace(BOB_MOTION, BOB_MOTION_WITH_POLICY);
    }
}
