package nsf.stress;

import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonObject;
import nsf.stress.model.HealthRecord;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.BiFunction;

public final class HealthRecordParser {

  /**
   * Parses the JSON-encoded health record.
   * <p>
   * The following are the expected JSON paths for health record attributes:
   *
   * <ul>
   *   <li><b>activeDuration</b>: activityData.summary.durations.active_seconds</li>
   *   <li><b>expenditureInKilocalories</b>: activityData.summary.energy_expenditure.active_kcal</li>
   *   <li><b>stepCount</b>: activityData.summary.movement.steps_count</li>
   *   <li><b>averageSpeedInKilometersPerHour</b>: activityData.summary.movement.speed.avg_km_h</li>
   *   <li><b>heartRatesInBeatsPerMinute</b>:
   *      <ul>
   *        <li>biometricsData.heart_rate.samples_bpm.value</li>
   *        <li>biometricsData.heart_rate.samples_bpm.time</li>
   *      </ul>
   *   <li><b>sleepDuration</b>: sleepData.durations.total_seconds</li>
   * </ul>
   *
   * @param json JSON-encoded health record
   * @return parsed health record
   * @throws IllegalArgumentException if a JSON key is not found
   * @throws ClassCastException       if a JSON value is not the expected type
   * @throws DateTimeParseException   if a JSON timestamp string value is not ISO-8601 encoded
   */
  public HealthRecord parse(JsonObject json) {
    return HealthRecord.builder()
        .activeDuration(getActiveDuration(json))
        .expenditureInKilocalories(getExpenditure(json))
        .stepCount(getStepCount(json))
        .averageSpeedInKilometersPerHour(getAverageSpeed(json))
        .heartRatesInBeatsPerMinute(getHeartRates(json))
        .sleepDuration(getSleepDuration(json))
        .build();
  }

  private static Duration getActiveDuration(JsonObject json) {
    return getValue(json, activeDurationPath(), HealthRecordParser::getDuration);
  }

  private static List<String> activeDurationPath() {
    return List.of("activityData", "summary", "durations", "active_seconds");
  }

  private static double getExpenditure(JsonObject json) {
    return getValue(json, expenditurePath(), JsonObject::getDouble);
  }

  private static List<String> expenditurePath() {
    return List.of("activityData", "summary", "energy_expenditure", "active_kcal");
  }

  private static long getStepCount(JsonObject json) {
    return getValue(json, stepCountPath(), JsonObject::getLong);
  }

  private static List<String> stepCountPath() {
    return List.of("activityData", "summary", "movement", "steps_count");
  }

  private static double getAverageSpeed(JsonObject json) {
    return getValue(json, speedPath(), JsonObject::getDouble);
  }

  private static List<String> speedPath() {
    return List.of("activityData", "summary", "movement", "speed", "avg_km_h");
  }

  private static NavigableMap<Instant, Integer> getHeartRates(JsonObject json) {
    var samples = getValue(json, heartRatesPath(), JsonObject::getJsonArray);
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

  private static Duration getSleepDuration(JsonObject json) {
    return getValue(json, sleepDurationPath(), HealthRecordParser::getDuration);
  }

  private static List<String> sleepDurationPath() {
    return List.of("sleepData", "durations", "total_seconds");
  }

  private static <T> T getValue(
      JsonObject json, String key, BiFunction<JsonObject, String, T> mapper) {
    return getValue(json, List.of(key), mapper);
  }

  private static <T> T getValue(
      JsonObject json, List<String> path, BiFunction<JsonObject, String, T> mapper) {
    var valueObject = json;
    var subPath = new StringJoiner(".");
    int valueKeyIndex = path.size() - 1;
    if (path.size() > 1) {
      for (var key : path.subList(0, valueKeyIndex)) {
        subPath.add(key);
        checkKey(json, valueObject, key, subPath.toString());
        valueObject = valueObject.getJsonObject(key);
      }
    }
    var valueKey = path.get(valueKeyIndex);
    subPath.add(valueKey);
    checkKey(json, valueObject, valueKey, subPath.toString());
    return mapper.apply(valueObject, valueKey);
  }

  private static void checkKey(JsonObject json, JsonObject part, String key, String path) {
    Preconditions.checkArgument(part.containsKey(key), "Key \"%s\" not found in %s", path, json);
  }

  private static Duration getDuration(JsonObject json, String key) {
    return Duration.ofSeconds(json.getLong(key));
  }
}
