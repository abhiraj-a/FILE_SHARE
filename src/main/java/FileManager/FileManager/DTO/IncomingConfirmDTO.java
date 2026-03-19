package FileManager.FileManager.DTO;

import lombok.Getter;

import java.util.List;

@Getter
public class IncomingConfirmDTO {
    private String uploadId;
    private String transferId;
    private String s3Key;
    private List<PartDto> part;
}
