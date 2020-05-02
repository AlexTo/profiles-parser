package ai.beu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {
    private String name;
    private String url;
    private String id;
    private Location location;
}
