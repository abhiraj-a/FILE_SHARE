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
    private List<ChunkUrl> chunkUrls;

    @Builder
    @Data
    public static class ChunkUrl{
        private String presignedUrl;
        private int partnumber;
    }

}
