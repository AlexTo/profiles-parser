package ai.beu.models.fbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Profile {
    private String id;
    private String name;
    private String gender;
    private PageLikes likes;
    private Location hometown;
    private String relationshipStatus;
    private Location location;
    private List<Education> education;
    private List<Work> work;
    private Friends friends;
    private Albums albums;
}
