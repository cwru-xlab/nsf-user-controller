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

  public abstract double distanceInMeters();

  public abstract double averageSpeedInKilometersPerHour();

  public abstract NavigableMap<Instant, Integer> heartRatesInBeatsPerMinute();

  public abstract Duration sleepDuration();

  @Value.Derived
  public double averageHeartRateReserve() {
    var maxHeartRate = maxHeartRateInBeatsPerMinute();
    return maxHeartRate == 0d ? 0d : averageHeartRate() / maxHeartRate;
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
    if (heartRatesInBeatsPerMinute().isEmpty()) {
      return 0;
    } else {
      double nAbove = heartRates().filter(bpm -> bpm > beatsPerMinute).count();
      return nAbove / heartRatesInBeatsPerMinute().size();
    }
  }

  @Value.Check
  protected void check() {
    checkIsNonNegative(activeDuration().getSeconds(), "activeDuration");
    checkIsNonNegative(expenditureInKilocalories(), "expenditureInKilocalories");
    checkIsNonNegative(stepCount(), "stepCount");
    checkIsNonNegative(distanceInMeters(), "distanceInMeters");
    checkIsNonNegative(averageSpeedInKilometersPerHour(), "averageSpeedInKilometersPerHour");
    heartRatesInBeatsPerMinute().values().forEach(hr -> checkIsNonNegative(hr, "heartRate"));
  }

  private IntStream heartRates() {
    return heartRatesInBeatsPerMinute().values().stream().mapToInt(Integer::valueOf);
  }

  private static void checkIsNonNegative(long value, String name) {
    Preconditions.checkState(value >= 0L, "'%s' must be non-negative; got %s", name, value);
  }

  private static void checkIsNonNegative(double value, String name) {
    Preconditions.checkState(value >= 0d, "'%s' must be non-negative; got %s", name, value);
  }
}
