package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Builder
@Data
public class DownloadFileDTO {

    private final Resource resource;
    private final String originalFileName;
    private final String content_type;
}
