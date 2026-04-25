package FileManager.FileManager.Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MailCongig {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
