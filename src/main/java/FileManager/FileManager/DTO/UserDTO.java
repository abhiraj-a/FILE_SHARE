package FileManager.FileManager.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDTO {

    private UUID id;
    private String email;
    private String name;

}
