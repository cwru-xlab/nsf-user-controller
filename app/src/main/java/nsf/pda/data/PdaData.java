package nsf.pda.data;

import io.vertx.core.json.JsonArray;

/**
 * Useless class for now that I made since I'm changing the internal datatype a lot
 * - could be refactored out in the future.
 */
public class PdaData {
  private JsonArray jsonNode;

  public PdaData(JsonArray jsonNode){
    this.jsonNode = jsonNode;
  }

  public JsonArray get() {
    return jsonNode;
  }
}
