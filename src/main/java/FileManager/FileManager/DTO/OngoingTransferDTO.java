package FileManager.FileManager.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OngoingTransferDTO {

    private String transferId;
    private String verificationCode;
    private int fileCount;
    private boolean isRevoked;
    private List<OngoingTransferDTO.FileMetaData> fileMetaDataList;
    private Instant expiresAt;

    @Data
    @Builder
    public static class FileMetaData{
        private UUID id;
        private String originalFileName;
        private long fileSize;
        private String contentType;

    }
}
