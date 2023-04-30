package nsf.stress.model;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import java.util.NavigableMap;
import java.util.stream.IntStream;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseHealthRecord {

  public abstract Duration activeDuration();

  public abstract double expenditureInKilocalories();

  public abstract long stepCount();

  public abstract double averageSpeedInKilometersPerHour();

  public abstract NavigableMap<Instant, Integer> heartRatesInBeatsPerMinute();

  public abstract Duration sleepDuration();

  @Value.Derived
  public double averageHeartRateReserve() {
    return safeDivide(averageHeartRate(), maxHeartRateInBeatsPerMinute());
  }

  @Value.Derived
  public int maxHeartRateInBeatsPerMinute() {
    return heartRates().max().orElse(0);
  }

  @Value.Derived
  public double averageHeartRate() {
    return heartRates().average().orElse(0d);
  }

  public double percentHeartRatesAbove(int beatsPerMinute) {
    double nAbove = heartRates().filter(bpm -> bpm > beatsPerMinute).count();
    return safeDivide(nAbove, heartRatesInBeatsPerMinute().size());
  }

  @Value.Check
  protected void check() {
    checkIsFiniteNonNegative(activeDuration().getSeconds(), "activeDuration");
    checkIsFiniteNonNegative(expenditureInKilocalories(), "expenditureInKilocalories");
    checkIsFiniteNonNegative(stepCount(), "stepCount");
    checkIsFiniteNonNegative(averageSpeedInKilometersPerHour(), "averageSpeedInKilometersPerHour");
    checkIsFiniteNonNegative(sleepDuration().getSeconds(), "sleepDuration");
    heartRatesInBeatsPerMinute().values().forEach(hr -> checkIsFiniteNonNegative(hr, "heartRate"));
  }

  private IntStream heartRates() {
    return heartRatesInBeatsPerMinute().values().stream().mapToInt(Integer::valueOf);
  }

  private static double safeDivide(double dividend, double divisor) {
    return divisor != 0d ? dividend / divisor : 0d;
  }

  private static void checkIsFiniteNonNegative(double value, String name) {
    boolean isValid = Double.isFinite(value) && value >= 0d;
    Preconditions.checkState(isValid, "\"%s\" must be finite and non-negative; got %s", name, value);
  }
}
