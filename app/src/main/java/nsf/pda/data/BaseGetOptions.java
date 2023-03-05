package nsf.pda.data;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Range;
import java.util.Optional;
import java.util.OptionalInt;
import org.immutables.value.Value;

@Value.Immutable
@JsonInclude(Include.NON_NULL)
abstract class BaseGetOptions {

  private static final Range<Integer> TAKE_RANGE = Range.closed(0, 1000);
  private static final Range<Integer> SKIP_RANGE = Range.atLeast(0);

  private static final String ORDER_BY_MSG = "'orderBy' must be a non-empty string";
  private static final String TAKE_MSG = "'take' must be in the range " + TAKE_RANGE;
  private static final String SKIP_MSG = "'skip' must be in the range " + SKIP_RANGE;

  public abstract Optional<String> orderBy();

  public abstract Optional<Ordering> ordering();

  public abstract OptionalInt take();

  public abstract OptionalInt skip();

  @Value.Check
  protected void check() {
    orderBy().ifPresent(orderBy -> checkArgument(orderBy.isEmpty(), ORDER_BY_MSG));
    take().ifPresent(take -> checkArgument(TAKE_RANGE.contains(take), TAKE_MSG));
    skip().ifPresent(skip -> checkArgument(SKIP_RANGE.contains(skip), SKIP_MSG));
  }
}
