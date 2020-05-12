package ai.beu.models.fbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class School {
    private String id;
    private String name;
}
