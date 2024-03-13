package nsf.access;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.UpdateOptions;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

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

  // TODO SERIALIZE DESERIALIZE POJO

//  public Future<List<JsonObject>> getAllServProvs(){
//    JsonObject query = new JsonObject();
//    return client().find(collection(), query);
//    //.compose(documents -> {
//    //      Promise<List<JsonObject>> promise = Promise.promise();
//    //      promise.complete(documents);
//    //      return promise.future();
//    //    })
//  }

  public Future<List<JsonObject>> listServProvs(){
    JsonObject query = new JsonObject();
    return client().find(collection(), query);
  }

  // TODO REFACTOR USE COMMON GUARD FUNC AND ALSO EVENTUALLY REFACTOR INTO POJO
  public Future<String> getServProvConnId(String servProvId){
    return getServProv(servProvId).compose(json -> {
      Promise<String> promise = Promise.promise();
      if (json.isPresent()){
        promise.complete(json.get().getString("connId"));
      }
      else{
       promise.fail(new ServProvNotFoundException());
      }
      return promise.future();
    });
  }
  public Future<JsonObject> getServProvData(String servProvId){
    return getServProv(servProvId).compose(json -> {
      Promise<JsonObject> promise = Promise.promise();
      if (json.isPresent()){
        promise.complete(json.get());
      }
      else{
        promise.fail(new ServProvNotFoundException());
      }
      return promise.future();
    });
  }

  public Future<Optional<JsonObject>> getServProv(String servProvId){
    JsonObject query = new JsonObject()
        .put("_id", servProvId);
    return client().findOne(collection(), query, null).compose(json -> {
      Promise<Optional<JsonObject>> promise = Promise.promise();
      promise.complete(Optional.ofNullable(json));
      return promise.future();
    });
  }

  /**
   * REMARK: Currently this should only be called when the service provider is initially added, as there seems to be
   * no reason that you would want to "rename" or change this mapping in the future (although that is possible if we
   * want).
   */
  public Future<String> setServProvConnId(String connId, String presentationExchangeId, JsonObject serverBannerData){
    JsonObject document = new JsonObject()
        .put("_id", connId)
        .put("connId", connId)
        .put("presentationExchangeId", presentationExchangeId)
        .put("bannerData", serverBannerData);
    return this.client().save(this.collection(), document);
  }

  public Future<MongoClientDeleteResult> deleteServProvConnMapping(String servProvId) {
    JsonObject query = new JsonObject()
        .put("_id", servProvId);
    return client().removeDocument(collection(), query);
  }
}
