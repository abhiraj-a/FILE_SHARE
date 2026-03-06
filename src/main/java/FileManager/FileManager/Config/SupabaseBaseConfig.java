package FileManager.FileManager.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;

//@Configuration
public class SupabaseBaseConfig {

//    @Value("${supabase.url}")
//    private String url;
//
//    @Value("${supabase.service-key}")
//    private String serviceKey;
//    @Bean
//    public WebClient supaBaseWebClient(){
//        return WebClient.builder()
//                .baseUrl(url)
//                .defaultHeader(HttpHeaders.AUTHORIZATION , "Bearer "+serviceKey)
//                .defaultHeader("apikey", serviceKey)
//                .build();
//    }
}
