package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ConfirmCompletedto {
    private String verificationCode;
    private Instant expiresAt;
    private String transferId;
}
