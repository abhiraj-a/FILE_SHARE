package FileManager.FileManager.ServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final RestTemplate restTemplate;
    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${mail.sender.email}")
    private String senderEmail;

    @Async
    @Transactional
    public void sendVerificationEmail(
            String toEmail,
            String name,
            String verificationToken
    ) {

        log.warn("send verification mail method reached");

        String body = """
        {
          "sender":{
            "name":"FileShare",
            "email":"%s"
          },
          "to":[{"email":"%s"}],
          "subject":"Verification code for a file transfer issued to you",
          "htmlContent":"<h2>Hello %s</h2><p>Your verification token:</p><b>%s  Please enter this code to recieve the tranferx</b>"
        }
        """.formatted(
                senderEmail,
                toEmail,
                name,
                verificationToken
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "https://api.brevo.com/v3/smtp/email",
                        request,
                        String.class
                );

        log.info("Brevo response: {}", response.getBody());
        log.info("Verification email sent to {}", toEmail);
    }
}
