package FileManager.FileManager.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PartDto {
    @JsonProperty("eTag")
    private String eTag;
    @JsonProperty("partnumber")
    private int partnumber;
}
