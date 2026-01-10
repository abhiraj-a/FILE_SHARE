package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.Service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final WebClient supabaseWebClient;

    @Value("${supabase.bucket}")
    private String bucket;

    @Override
    public String upload(MultipartFile file, UUID ownerId , String path) {

        log.info("RAW path before upload = [{}]", path);

        try {
            supabaseWebClient.post()
                    .uri("/storage/v1/object/" + bucket + "/" + path)
                    .header(HttpHeaders.CONTENT_TYPE,file.getContentType())
                    .header("x-upsert", "true")
                    .bodyValue(file.getBytes())
                    .retrieve().toBodilessEntity()
                    .block();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path;
    }

    @Override
    public String generateSignedDownload(String path) {

        Map<String, Object> body = new HashMap<>();
        body.put("expiresIn", 300);

        String encodedpath = UriUtils.encodePath(path, StandardCharsets.UTF_8);
        return supabaseWebClient
                .post()
//                .uri("/storage/v1/object/sign/{bucket}/{path}", bucket, path)
                .uri(uriBuilder ->
                uriBuilder
                        .path("/storage/v1/object/sign/")
                        .path(bucket)
                        .path("/")
                        .path(path)
                        .build()
                )
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("signedURL").asText())
                .block();

    }

    @Override
    public void delete(String path) {
        supabaseWebClient.delete()
                .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
