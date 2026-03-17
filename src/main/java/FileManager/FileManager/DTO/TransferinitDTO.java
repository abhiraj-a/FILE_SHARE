package FileManager.FileManager.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TransferinitDTO {
    private String originalFileName;
    private long fileSize;
    private String fileType;
    private String contentType;
}
