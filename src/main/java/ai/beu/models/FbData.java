package ai.beu.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FbData {
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private Address address;
    private String birthday;
    private String adminNotes;
    private String gender;
    private String email;
    private String link;
    private Location location;
    private String interestedIn;
    private String significantOther;
    private String shortName;
    private String website;
    private NodeSet groups;
    private NodeSet adminedGroups;
    private Place hometown;
    private NodeSet albums;
    private NodeSet businesses;
    private NodeSet friends;
    private NodeSet liveVideos;
}
