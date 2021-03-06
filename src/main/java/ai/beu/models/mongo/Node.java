package ai.beu.models.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
    private String url;
    private String name;
    private String id;
    private Title title;
    private Title message;
    private Image image;
}
