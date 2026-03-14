package FileManager.FileManager.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class PreSignedDTO {

    private String url;
    private String originalFileName;

}
