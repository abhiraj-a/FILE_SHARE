package FileManager.FileManager.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ClerkClient {

    private final WebClient  webClient;

    public ClerkClient(@Value("${clerk.secret-key}") String clerkSecretKey){
        this.webClient = WebClient.builder()
                .baseUrl("https://api.clerk.com/v1")
                .defaultHeader("Authorization" , "Bearer "+clerkSecretKey)
                .build();
    }

    public void deleteUser(ClerkUserPrincipal principal){
        webClient.delete()
                .uri("/users/{id}" , principal.getClerkId())
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
