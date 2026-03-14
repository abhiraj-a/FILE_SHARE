package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Data
public class TransferInitResponse {
    private List<PreSignedDTO> preSignedDTO;
    private String verificationCode;
    private String transferId;
}
