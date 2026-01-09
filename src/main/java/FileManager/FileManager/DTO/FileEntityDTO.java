package FileManager.FileManager.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileEntityDTO {

    private UUID fileId;

    private String originalFileName;

    private long fileSize;

    private Instant uploadedAt;
}
