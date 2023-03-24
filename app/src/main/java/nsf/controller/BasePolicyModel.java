package nsf.controller;

import nsf.access.Operation;
import nsf.access.Policy;
import org.immutables.value.Value;

import java.util.LinkedList;

/**
 * Request model for a Service Provider access control policy. (used as request body deserialized object).
 */
@Value.Immutable
public abstract class BasePolicyModel {
  public abstract LinkedList<Operation> operations();

  public abstract LinkedList<String> resources();

  /**
   * Converts Policy request model to database entity given the Service Provider ID.
   */
  public Policy toEntity(String serviceProviderId){
    return Policy.builder()
        .serviceProviderId(serviceProviderId)
        .version("0.1.0")
        .operations(operations())
        .resources(resources())
        .build();
  }
}
