package nsf.stress;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nsf.stress.model.HealthRecord;
import org.immutables.builder.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class HealthRecords {

  private static final long ACTIVE_DURATION_LONG = 123L;
  private static final Duration ACTIVE_DURATION = Duration.ofSeconds(ACTIVE_DURATION_LONG);
  private static final double AVERAGE_SPEED = 4.20d;
  private static final double EXPENDITURE = 2001.3d;
  private static final long STEP_COUNT = 10_002L;
  private static final Instant HR_TIME = Instant.EPOCH;
  private static final int HR_VALUE = 65;
  private static final Map<Instant, Integer> HR_MAP = Map.of(HR_TIME, HR_VALUE);
  private static final NavigableMap<Instant, Integer> HR_TREE_MAP = new TreeMap<>(HR_MAP);
  private static final long SLEEP_DURATION_LONG = 4567L;
  private static final Duration SLEEP_DURATION = Duration.ofSeconds(SLEEP_DURATION_LONG);

  private static final Object INVALID_TYPE = new Object();
  private static final Map<?, ?> INVALID_HR_TIME_TYPE = Map.of(INVALID_TYPE, HR_VALUE);
  private static final Map<?, ?> INVALID_HR_TIME_ENCODING = Map.of("2023-01-01", HR_VALUE);
  private static final Map<?, ?> INVALID_HR_VALUE_TYPE = Map.of(HR_TIME, INVALID_TYPE);

  private HealthRecords() {
  }

  public static HealthRecord.Builder builderWithInfiniteAverageSpeed() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(Double.POSITIVE_INFINITY)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithInfiniteExpenditure() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(Double.POSITIVE_INFINITY)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNaNAverageSpeed() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(Double.NaN)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNaNExpenditure() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(Double.NaN)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeActiveDuration() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION.negated())
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeAverageSpeed() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(-AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeExpenditure() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(-EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeHeartRates() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(-AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(new TreeMap<>(Map.of(HR_TIME, -1)))
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeSleepDuration() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION.negated())
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithNegativeStepCount() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION.negated())
        .stepCount(-STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingActiveDuration() {
    return HealthRecord.builder()
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingAverageSpeed() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingExpenditure() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingHeartRates() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingSleepDuration() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .stepCount(STEP_COUNT);
  }

  public static HealthRecord.Builder builderWithMissingStepCount() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION);
  }

  public static JsonObject jsonWithInvalidActiveDurationType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(INVALID_TYPE)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidAverageSpeedType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(INVALID_TYPE)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidExpenditureType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(INVALID_TYPE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidSleepDurationType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(INVALID_TYPE)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidHeartRatesType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(INVALID_TYPE)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidHeartRatesTimeEncoding() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(INVALID_HR_TIME_ENCODING)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidHeartRatesTimeType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(INVALID_HR_TIME_TYPE)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidHeartRatesValueType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(INVALID_HR_VALUE_TYPE)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithInvalidStepCountType() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(INVALID_TYPE)
        .build();
  }

  public static JsonObject jsonWithMissingActiveDuration() {
    return HealthRecordJsonBuilder.create()
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithMissingAverageSpeed() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithMissingExpenditure() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithMissingHeartRates() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithMissingSleepDuration() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .stepCount(STEP_COUNT)
        .build();
  }

  public static JsonObject jsonWithMissingStepCount() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .build();
  }

  public static HealthRecord.Builder validBuilder() {
    return HealthRecord.builder()
        .activeDuration(ACTIVE_DURATION)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_TREE_MAP)
        .sleepDuration(SLEEP_DURATION)
        .stepCount(STEP_COUNT);
  }

  public static JsonObject validJson() {
    return HealthRecordJsonBuilder.create()
        .activeDuration(ACTIVE_DURATION_LONG)
        .averageSpeedInKilometersPerHour(AVERAGE_SPEED)
        .expenditureInKilocalories(EXPENDITURE)
        .heartRatesInBeatsPerMinute(HR_MAP)
        .sleepDuration(SLEEP_DURATION_LONG)
        .stepCount(STEP_COUNT)
        .build();
  }

  @Builder.Factory
  static JsonObject healthRecordJson(
      Optional<Object> activeDuration,
      Optional<Object> averageSpeedInKilometersPerHour,
      Optional<Object> expenditureInKilocalories,
      Optional<Object> heartRatesInBeatsPerMinute,
      Optional<Object> sleepDuration,
      Optional<Object> stepCount) {
    var json = new JsonObject();
    addActivityData(
        activeDuration,
        averageSpeedInKilometersPerHour,
        expenditureInKilocalories,
        stepCount,
        json);
    addBiometricsData(heartRatesInBeatsPerMinute, json);
    addSleepData(sleepDuration, json);
    return json;
  }

  private static void addActivityData(
      Optional<Object> activeDuration,
      Optional<Object> averageSpeedInKilometersPerHour,
      Optional<Object> expenditureInKilocalories,
      Optional<Object> stepCount,
      JsonObject json) {
    var activityData = new JsonObject();
    var summary = new JsonObject();
    var movement = new JsonObject();
    activeDuration.ifPresent(duration ->
        summary.put("durations", JsonObject.of("active_seconds", duration)));
    expenditureInKilocalories.ifPresent(expenditure ->
        summary.put("energy_expenditure", JsonObject.of("active_kcal", expenditure)));
    stepCount.ifPresent(count -> movement.put("steps_count", count));
    averageSpeedInKilometersPerHour.ifPresent(speed ->
        movement.put("speed", JsonObject.of("avg_km_h", speed)));
    summary.put("movement", movement);
    activityData.put("summary", summary);
    json.put("activityData", activityData);
  }

  private static void addBiometricsData(
      Optional<Object> heartRatesInBeatsPerMinute, JsonObject json) {
    heartRatesInBeatsPerMinute.ifPresent(heartRates -> {
      var samples = new JsonArray();
      if (heartRates instanceof Map) {
        ((Map<?, ?>) heartRates).entrySet().stream()
            .map(entry -> JsonObject.of("time", entry.getKey(), "value", entry.getValue()))
            .forEach(samples::add);
      } else {
        samples.add(heartRates);
      }
      var biometricsData = JsonObject.of("heart_rate", JsonObject.of("samples_bpm", samples));
      json.put("biometricsData", biometricsData);
    });
  }

  private static void addSleepData(Optional<Object> sleepDuration, JsonObject json) {
    sleepDuration.ifPresent(duration -> {
      var durations = JsonObject.of("total_seconds", duration);
      json.put("sleepData", JsonObject.of("durations", durations));
    });
  }
}
