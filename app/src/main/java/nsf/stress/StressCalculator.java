package nsf.stress;


import java.time.Clock;
import java.util.stream.DoubleStream;
import nsf.stress.model.HealthRecord;
import nsf.stress.model.StressScore;

public final class StressCalculator {

  private static final double SECONDS_PER_DAY = 86400;
  private static final double RECOMMENDED_INTAKE_IN_KILOCALORIES = 2000;
  private static final double METERS_PER_KILOMETER = 1000;
  private static final double TARGET_MAX_HEART_RATE = 200;
  private static final int HEART_RATE_SPIKE_THRESHOLD = 80;
  private static final double RECOMMENDED_SLEEP_IN_SECONDS = 28800;
  private static final double SCALING = 10;

  public final Clock clock;

  public StressCalculator(Clock clock) {
    this.clock = clock;
  }

  public StressScore calculate(HealthRecord record) {
    return StressScore.builder()
        .value(Math.min(calculateRawValue(record), 100d))
        .timestamp(clock.instant())
        .build();
  }
  
  private static double calculateRawValue(HealthRecord record) {
    return SCALING * DoubleStream.of(
            record.activeDuration().getSeconds() / SECONDS_PER_DAY,
            record.expenditureInKilocalories() / RECOMMENDED_INTAKE_IN_KILOCALORIES,
            record.averageSpeedInKilometersPerHour() / METERS_PER_KILOMETER,
            record.maxHeartRateInBeatsPerMinute() / TARGET_MAX_HEART_RATE,
            record.averageHeartRateReserve(),
            record.percentHeartRatesAbove(HEART_RATE_SPIKE_THRESHOLD),
            record.sleepDuration().getSeconds() / RECOMMENDED_SLEEP_IN_SECONDS)
        .sum();
  }
}
