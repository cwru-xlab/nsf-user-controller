package nsf.controller;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Response {

  public abstract Optional<Data> data();

  public abstract Optional<Error> error();

  @Value.Check
  protected void check() {
    Preconditions.checkArgument(
        data().isPresent() ^ error().isPresent(),
        "Exactly one of the following must be present: 'data' or 'error'");
  }
}
