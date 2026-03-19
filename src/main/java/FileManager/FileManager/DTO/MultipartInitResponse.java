package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class MultipartInitResponse {
    private String uploadId;
    private String s3Key;
    private List<String> presignedUrls;

}
