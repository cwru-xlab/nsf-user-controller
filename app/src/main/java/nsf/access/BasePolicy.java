package nsf.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class BasePolicy {

    @JsonProperty("_id")
    public abstract String serviceProviderId();

    public abstract String version();

    public abstract List<Operation> operations();

    public abstract List<String> resources();

}
