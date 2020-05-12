package ai.beu.models.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    @JsonProperty("STT")
    private String stt;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("UID")
    private Long uid;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Birthday")
    private String birthDay;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("SDT")
    private String sdt;

    @JsonProperty("Location")
    private String location;
}
