package nsf.pda;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.Range;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseGetOptions {

  private static final Range<Integer> TAKE_RANGE = Range.closed(0, 1000);
  private static final Range<Integer> SKIP_RANGE = Range.atLeast(0);

  private static final String ORDER_BY_MSG = "'orderBy' must be a non-empty string";
  private static final String TAKE_MSG = "'take' must be in the range " + TAKE_RANGE;
  private static final String SKIP_MSG = "'skip' must be in the range " + SKIP_RANGE;

  public abstract String orderBy();

  public abstract Ordering ordering();

  public abstract int take();

  public abstract int skip();

  protected void check() {
    checkArgument(orderBy().isEmpty(), ORDER_BY_MSG);
    checkArgument(TAKE_RANGE.contains(take()), TAKE_MSG);
    checkArgument(SKIP_RANGE.contains(skip()), SKIP_MSG);
  }
}
