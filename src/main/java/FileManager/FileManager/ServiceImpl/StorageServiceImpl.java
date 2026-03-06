package FileManager.FileManager.ServiceImpl;
import FileManager.FileManager.Service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

//

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;


    @Override
    public String upload(MultipartFile file, UUID ownerId , String path) {

        log.info("RAW path before upload = [{}]", path);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(),file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return path;

//        try {
//            supabaseWebClient.post()
//                    .uri("/storage/v1/object/" + bucket + "/" + path)
//                    .header(HttpHeaders.CONTENT_TYPE,file.getContentType())
//                    .header("x-upsert", "true")
//                    .bodyValue(file.getBytes())
//                    .retrieve().toBodilessEntity()
//                    .block();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return path;
    }

    @Override
    public String generateSignedDownload(String path, String originalFileName) {

        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + originalFileName + "\"; filename*=UTF-8''" + encodedFileName;
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .responseContentDisposition(contentDisposition)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(5))
                .build();

        return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();


//
//        Map<String, Object> body = new HashMap<>();
//        body.put("expiresIn", 300);
//        body.put("download" , true);
//
//        return supabaseWebClient
//                .post()
//                .uri("/storage/v1/object/sign/{bucket}/{path}", bucket, path)
//                .uri(uriBuilder ->
//                uriBuilder
//                        .path("/storage/v1/object/sign/")
//                        .path(bucket)
//                        .path("/")
//                        .path(path)
//                        .build()
//                )
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .map(json -> json.get("signedURL").asText())
//                .block();

    }

    @Override
    public void delete(String path) {

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
//        supabaseWebClient.delete()
//                .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
//                .retrieve()
//                .toBodilessEntity()
//                .block();
    }
}
