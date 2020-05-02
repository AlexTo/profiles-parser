package ai.beu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private String url;
    private Integer width;
    private Integer height;
}
