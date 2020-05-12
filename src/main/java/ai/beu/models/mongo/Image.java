package ai.beu.models.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private String url;
    private Integer width;
    private Integer height;
}
