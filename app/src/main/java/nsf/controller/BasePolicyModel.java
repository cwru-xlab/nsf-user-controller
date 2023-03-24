package nsf.controller;

import nsf.access.Operation;
import nsf.access.Policy;
import org.immutables.value.Value;

import java.util.ArrayList;

/**
 * Request model for a Service Provider access control policy. (used as request body deserialized object).
 */
@Value.Immutable
public abstract class BasePolicyModel {
  public abstract ArrayList<Operation> operations();

  public abstract ArrayList<String> resources();

  /**
   * Converts Policy request model to database entity given the Service Provider ID.
   */
  public Policy toEntity(String serviceProviderId){
    return Policy.builder()
        .serviceProviderId(serviceProviderId)
        .version("0.1.0")
        .addAllOperations(operations())
        .addAllResources(resources())
        .build();
  }
}
