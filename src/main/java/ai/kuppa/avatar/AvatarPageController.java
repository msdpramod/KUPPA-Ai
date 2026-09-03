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
    private static final String CONTINUITY_ADAPTER_JS = "<script src=\"/kuppa-continuity-adapter.js\"></script>";
    private static final String TRUSTED_DEVICES_JS = "<script src=\"/trusted-devices.js\"></script>";

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String avatar() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String html;
        try (var input = resource.getInputStream()) {
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        return html.replace("</head>", TRUSTED_DEVICES_CSS + "</head>")
                .replace("</body>", CONTINUITY_ADAPTER_JS + TRUSTED_DEVICES_JS + "</body>");
    }
}
