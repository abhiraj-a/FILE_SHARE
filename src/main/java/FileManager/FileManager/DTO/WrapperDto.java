package FileManager.FileManager.DTO;

import lombok.Getter;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Getter
public class WrapperDto {
    private List<TransferinitDTO> files ;
    private BeneficiaryDTO beneficiaryDTO;
}
