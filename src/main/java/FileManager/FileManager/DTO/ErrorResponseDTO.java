package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class ErrorResponseDTO {

    private String error;
    private String message;
    private int status;
    private Instant timestamp;
    private String path;
}
