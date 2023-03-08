package nsf.pda.data;

import com.fasterxml.jackson.databind.JsonNode;
import org.immutables.value.Value;

@Value.Immutable
public abstract class BaseRecord {

  public static Record from(JsonNode jsonNode) {
    return Record.builder()
        .recordId(jsonNode.get("recordId").asText())
        .endpoint(jsonNode.get("endpoint").asText())
        .data(jsonNode.get("data"))
        .build();
  }

  public abstract String endpoint();

  public abstract String recordId();

  public abstract JsonNode data();
}
