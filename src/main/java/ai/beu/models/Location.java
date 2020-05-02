package ai.beu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private Float latitude;
    private Float longitude;
}
