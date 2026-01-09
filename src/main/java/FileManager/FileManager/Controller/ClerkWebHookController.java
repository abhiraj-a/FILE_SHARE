package FileManager.FileManager.Controller;
import FileManager.FileManager.Service.UserService;
import FileManager.FileManager.Utils.ClerkUserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svix.Webhook;
import com.svix.exceptions.WebhookVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.http.HttpHeaders;
import java.util.*;


@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class ClerkWebHookController {

    @Value("${clerk.secret-key}")
    private String secretKey;
    private final UserService userService;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerk(
            @RequestHeader("svix-id")String svixId,
            @RequestHeader("svix-signature")String svixSignature,
            @RequestHeader("svix-timestamp")String svixTimestamp,
            @RequestBody String payload
    ){

        try {
            verifyWebhook(svixId,svixSignature,svixTimestamp,payload);

            ObjectMapper mapper =new ObjectMapper();

            JsonNode node = mapper.readTree(payload);

            String eventType = node.path("type").asText();

            if(eventType.equals("user.deleted")){
                userService.deleteUserByClerkId(node.path("data").get("id").asText());
            }
            if(eventType.equals("user.created")){
                String email = null;
                for (var e : node.path("data").path("email_addresses")){
                    if(e.get("primary").asBoolean()){
                        email = e.path("email_address").asText();
                        break;
                    }
                }
                ClerkUserPrincipal principal = ClerkUserPrincipal.builder()
                        .clerkId(node.path("data").get("id").asText())
                        .email(email)
                        .build();
                userService.signup(principal);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }


    private void verifyWebhook(String svixId, String svixSignature, String svixTimestamp, String payload)
            throws WebhookVerificationException {

        Webhook wh = new Webhook(secretKey);
        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("svix-id", Collections.singletonList(svixId));
        headerMap.put("svix-timestamp", Collections.singletonList(svixTimestamp));
        headerMap.put("svix-signature", Collections.singletonList(svixSignature));

        HttpHeaders headers = HttpHeaders.of(headerMap, (k, v) -> true);

        wh.verify(payload, headers);
    }
}