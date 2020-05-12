package ai.beu.models.fbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Clazz {
    private String id;
    private String name;
    private List<Profile> with;
}
