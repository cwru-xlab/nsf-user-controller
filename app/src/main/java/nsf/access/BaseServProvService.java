package nsf.access;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.immutables.value.Value;

/**
 * Service/repository for mediating access to persistent Service Provider mappings between the controller and database.
 * "Service Provider mapping" refers to how we map custom given Service Provider IDs to corresponding ACA-Py
 * connection IDs. We may also want to store additional metadata for each service provider rather than just the
 * mapped connection ID. Currently, each singular ID to ID mapping is stored as a separate document, but feel free to
 * change this.
 */
@Value.Immutable
public abstract class BaseServProvService {
  public abstract String collection();
  public abstract MongoClient client();


  public Future<String> getServProvConnId(String servProvId){
    JsonObject query = new JsonObject()
        .put(servProvId, new JsonObject().put("$exists", true));
    return client().findOne(collection(), query, null).compose(json -> {
      Promise<String> promise = Promise.promise();
      promise.complete(json.getString("connId"));
      return promise.future();
    });
  }

  /**
   * REMARK: Currently this should only be called when the service provider is initially added, as there seems to be
   * no reason that you would want to "rename" or change this mapping in the future (although that is possible if we
   * want).
   */
  public Future<String> setServProvConnId(String servProvId, String connId){
    JsonObject document = new JsonObject().put("_id", servProvId).put("connId", connId);
    return this.client().save(this.collection(), document);
  }

}
