package nsf.access;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class BaseServiceProvider {

    public abstract String version();

    public abstract String serviceProviderId();

    public abstract List<Policy> policies();
}
