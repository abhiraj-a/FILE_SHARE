package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Builder
@Data
@Getter
public class InitMultiDTO {
    private String transferId;
    private List<MultipartInitResponse> multipartInitResponseList;
}
