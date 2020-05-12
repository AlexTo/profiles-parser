package ai.beu.models.fbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Calendar;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Album {
    private String id;
    private String coverPhoto;
    private String name;
    private String type;
    private String link;
    private Calendar createdTime;
    private Calendar updatedTime;
}
