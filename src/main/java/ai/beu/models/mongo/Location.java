package ai.beu.models.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private Float latitude;
    private Float longitude;
}
