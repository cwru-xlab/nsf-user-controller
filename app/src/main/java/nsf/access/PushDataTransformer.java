package nsf.access;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import nsf.stress.HealthRecordParser;
import nsf.stress.StressCalculator;
import nsf.stress.model.HealthRecord;
import nsf.stress.model.StressScore;

import java.time.Clock;

/**
 * Transforms incoming pushable data to transform any data before being pushed to Service Providers. For example to
 * add any resources that Service Providers may be subscribed to and expecting.
 */
public class PushDataTransformer {

  public static JsonObject transformPushableData(JsonObject dataPlugJson){
    JsonObject dataPlugJsonCopy = dataPlugJson.copy();
    JsonObject healthRecordJson = healthRecordJsonFromDataPlug(dataPlugJsonCopy);

    HealthRecordParser parser = new HealthRecordParser();
    HealthRecord record = parser.parse(healthRecordJson);
    StressCalculator calculator = new StressCalculator(Clock.systemUTC());
    StressScore stressScore = calculator.calculate(record);

    dataPlugJsonCopy.put("stress-score-data", JsonObject.mapFrom(stressScore)); // ClassCast error in MongoDB save
    return dataPlugJsonCopy;
  }

  /**
   * Adapts Data Plug JSON to Health Record JSON -- Ideally in the future these two schemas could fully match up such
   * that this method may no longer be needed.
   */
  private static JsonObject healthRecordJsonFromDataPlug(JsonObject dataPlugJson) {
    JsonObject rawHealthData = dataPlugJson.getJsonObject("raw-health-data");

    { // Change time sample zone offset to abbr
      JsonArray heartRateSamples = rawHealthData.getJsonObject("biometrics")
          .getJsonObject("heart_rate")
          .getJsonArray("samples_bpm");
      for (Object sample : heartRateSamples){
        JsonObject sampleObj = (JsonObject) sample;
        String fixedTime = sampleObj.getString("time").replace("+00:00", "Z");
        sampleObj.put("time", fixedTime);
      }
    }

    return new JsonObject()
        .put("activityData", rawHealthData.getJsonObject("activity"))
        .put("biometricsData", rawHealthData.getJsonObject("biometrics"))
        .put("sleepData", rawHealthData.getJsonObject("sleep"));
  }
}
