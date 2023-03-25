package nsf.access;

import io.vertx.core.json.JsonObject;

/**
 * Transforms incoming pushable data to transform any data before being pushed to Service Providers. For example to
 * add any resources that Service Providers may be subscribed to and expecting.
 */
public class PushDataTransformer {

  public static JsonObject transformPushableData(JsonObject pushableData){
    JsonObject transformedPushData = pushableData.copy();

    JsonObject stressScoreData = new JsonObject();
    if (transformedPushData.containsKey("heartbeat-data")){
      JsonObject heartbeatData = transformedPushData.getJsonObject("heartbeat-data");
      if (heartbeatData.containsKey("wee")){
        stressScoreData.put("wee", heartbeatData.getString("wee") + " anonymized - (this is all just a " +
            "placeholder for the real stress score calculation)");
      }
      transformedPushData.put("stress-score-data", stressScoreData);
    }

    return transformedPushData;
  }
}
