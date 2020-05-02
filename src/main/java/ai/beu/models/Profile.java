package ai.beu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    private String name;
    private String desc;
    private String stt;
    private String gender;
    private String birthday;
    private String email;
    private String location;
    private String phone;
    private String avatar;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("updated_at")
    private Long updated_At;

    private ai.beu.models.Data data;

    private String uid;

    private String __v;

    @JsonProperty("crawled_at")
    private Long crawledAt;

    private Float crawled;

    private String type;

    private List<String> types;

    @JsonProperty("phonecheck")
    private Float phoneCheck;

    @JsonProperty("fbdata")
    private FbData fbData;

}
