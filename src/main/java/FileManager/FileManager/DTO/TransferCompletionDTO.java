package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TransferCompletionDTO {
    private String verificationCode;
    private  Instant  expiresAt;
    private String transferId;
}
