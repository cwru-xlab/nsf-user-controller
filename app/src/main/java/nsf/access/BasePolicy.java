package nsf.access;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class BasePolicy {

    public abstract List<Operation> operations();

    public abstract List<String> resources();

}
