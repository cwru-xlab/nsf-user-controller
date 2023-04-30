package nsf.stress.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.time.Instant;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseStressScore {

  public static final double MIN_VALUE = 0d;
  public static final double MAX_VALUE = 100d;

  private static final Range<Double> VALUE_RANGE = Range.closed(MIN_VALUE, MAX_VALUE);

  public abstract Instant timestamp();

  public abstract double value();

  @Value.Check
  protected void check() {
    Preconditions.checkState(
        isValueInRange(), "\"value\" must be in the range %s; got %s", VALUE_RANGE, value());
  }

  private boolean isValueInRange() {
    return VALUE_RANGE.contains(value());
  }
}
