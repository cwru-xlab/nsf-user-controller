package nsf.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.LinkedList;

@Value.Immutable
public abstract class BasePolicy {

    @JsonProperty("_id")
    public abstract String serviceProviderId();

    public abstract String version();

    public abstract LinkedList<Operation> operations();

    public abstract LinkedList<String> resources();

}
