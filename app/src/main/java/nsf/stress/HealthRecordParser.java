package nsf.stress;

import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonObject;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.BiFunction;
import nsf.stress.model.HealthRecord;

public final class HealthRecordParser {

  private static final HealthRecordParser INSTANCE = new HealthRecordParser();

  public static HealthRecordParser getInstance() {
    return INSTANCE;
  }

  /**
   * Parses the JSON-encoded health record.
   * <p>
   * The following are the expected JSON paths for health record attributes:
   *
   * <ul>
   *   <li><b>activeDuration</b>: activityData.summary.durations.active_seconds</li>
   *   <li><b>expenditureInKilocalories</b>: activityData.summary.energy_expenditure.active_kcal</li>
   *   <li><b>distanceInMeters</b>: activityData.summary.movement.distance_meters</li>
   *   <li><b>stepCount</b>: activityData.summary.movement.step_count</li>
   *   <li><b>averageSpeedInKilometersPerHour</b>: activityData.summary.movement.speed.avg_km_h</li>
   *   <li><b>heartRatesInBeatsPerMinute</b>:
   *      <ul>
   *        <li>biometricsData.heart_rate.samples_bpm.value</li>
   *        <li>biometricsData.heart_rate.samples_bpm.time</li>
   *      </ul>
   *   <li><b>sleepDuration</b>: sleepData.durations.total_seconds</li>
   * </ul>
   *
   * @param object JSON-encoded health record
   * @return parsed health record
   * @throws IllegalArgumentException if a JSON key is not found
   * @throws ClassCastException       if a JSON value is not the expected type
   * @throws DateTimeParseException   if a JSON value is not ISO-8601 encoded
   */
  public HealthRecord parse(JsonObject object) {
    return HealthRecord.builder()
        .activeDuration(getActiveDuration(object))
        .expenditureInKilocalories(getExpenditureInKilocalories(object))
        .stepCount(getStepCount(object))
        .averageSpeedInKilometersPerHour(getAverageSpeedInKilometersPerHour(object))
        .heartRatesInBeatsPerMinute(getHeartRatesInBeatsPerMinute(object))
        .sleepDuration(getSleepDuration(object))
        .build();
  }

  private static Duration getActiveDuration(JsonObject object) {
    return getValue(object, activeDurationPath(), HealthRecordParser::getDuration);
  }

  private static List<String> activeDurationPath() {
    return List.of("activityData", "summary", "durations", "active_seconds");
  }

  private static double getExpenditureInKilocalories(JsonObject object) {
    return getValue(object, expenditurePath(), JsonObject::getDouble);
  }

  private static List<String> expenditurePath() {
    return List.of("activityData", "summary", "energy_expenditure", "active_kcal");
  }

  private static long getStepCount(JsonObject object) {
    return getValue(object, stepCountPath(), JsonObject::getLong);
  }

  private static List<String> stepCountPath() {
    return List.of("activityData", "summary", "movement", "distance_meters");
  }

  private static double getAverageSpeedInKilometersPerHour(JsonObject object) {
    return getValue(object, speedPath(), JsonObject::getDouble);
  }

  private static List<String> speedPath() {
    return List.of("activityData", "summary", "movement", "speed", "avg_km_h");
  }

  private static NavigableMap<Instant, Integer> getHeartRatesInBeatsPerMinute(JsonObject object) {
    var samples = getValue(object, heartRatesPath(), JsonObject::getJsonArray);
    var heartRates = new TreeMap<Instant, Integer>();
    for (int i = 0; i < samples.size(); i++) {
      var heartRate = samples.getJsonObject(i);
      var timestamp = getValue(heartRate, "time", JsonObject::getInstant);
      var beatsPerMinute = getValue(heartRate, "value", JsonObject::getInteger);
      heartRates.put(timestamp, beatsPerMinute);
    }
    return heartRates;
  }

  private static List<String> heartRatesPath() {
    return List.of("biometricsData", "heart_rate", "samples_bpm");
  }

  private static Duration getSleepDuration(JsonObject object) {
    return getValue(object, sleepDurationPath(), HealthRecordParser::getDuration);
  }

  private static List<String> sleepDurationPath() {
    return List.of("sleepData", "durations", "total_seconds");
  }

  private static <T> T getValue(
      JsonObject object, String key, BiFunction<JsonObject, String, T> mapper) {
    return getValue(object, List.of(key), mapper);
  }

  private static <T> T getValue(
      JsonObject object, List<String> path, BiFunction<JsonObject, String, T> mapper) {
    var valueObject = object;
    var subPath = new StringJoiner(".");
    int valueKeyIndex = path.size() - 1;
    if (path.size() > 1) {
      for (var key : path.subList(0, valueKeyIndex)) {
        subPath.add(key);
        checkKey(object, valueObject, key, subPath.toString());
        valueObject = valueObject.getJsonObject(key);
      }
    }
    var valueKey = path.get(valueKeyIndex);
    subPath.add(valueKey);
    checkKey(object, valueObject, valueKey, subPath.toString());
    return mapper.apply(valueObject, valueKey);
  }

  private static void checkKey(JsonObject object, JsonObject inner, String key, String path) {
    Preconditions.checkArgument(
        inner.containsKey(key), "Key \"%s\" not found in object: %s", path, object);
  }

  private static Duration getDuration(JsonObject object, String key) {
    return Duration.ofSeconds(object.getLong(key));
  }
}
