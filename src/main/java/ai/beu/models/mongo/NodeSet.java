package ai.beu.models.mongo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeSet {
    private List<Node> nodes;
}
