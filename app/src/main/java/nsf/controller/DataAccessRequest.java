package nsf.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Access Request model (sent from service providers).
 */
public class DataAccessRequest {
  public enum DataAccessOperation {
    READ,
    WRITE,
    DELETE;
  }

  private DataAccessOperation operation;
  @JsonIgnore
  private DataResourceIdentifier resourceIdentifier;

  public DataAccessOperation getOperation() {
    return operation;
  }

  public void setOperation(DataAccessOperation operation) {
    this.operation = operation;
  }

  public DataResourceIdentifier getResourceIdentifier() {
    return resourceIdentifier;
  }

  @JsonProperty("resourceUri")
  public void setResourceUri(String resourceUri) throws Exception {
    this.resourceIdentifier = new DataResourceIdentifier(resourceUri);
  }
}
