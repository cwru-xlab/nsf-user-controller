package nsf.access;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class BaseServiceProvider {

    public abstract String version();

    @JsonProperty("_id")
    public abstract String serviceProviderId();

    public abstract List<Policy> policies();
}
