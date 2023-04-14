package nsf.access;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Value.Immutable
public abstract class BaseDataService {
  private static final Logger logger = LoggerFactory.getLogger(BaseDataService.class);
  public static final String collectionPrefix = "data.";
  public abstract MongoClient client();

  /**
   * Reads all policies that are subscribed (have the {@link Operation#SUBSCRIBE} operation).
   */
  public Future<List<Policy>> readAllSubscribePolicies(){
//    JsonObject query = new JsonObject()
//        .put("operations", new JsonObject().put("$in", new JsonArray().add("SUBSCRIBE")));
//    return client().find(collection(), query).compose(documents -> {
//      Promise<List<Policy>> promise = Promise.promise();
//      promise.complete(documents.stream().map(document -> document.mapTo(Policy.class)).collect(Collectors.toList()));
//      return promise.future();
//    });
    return null;
  }

  /**
   * Adds new JSON data. This data must follow the schema where the first layer objects (immediate children of the
   * given/root new data object) are for the different namespaces being updated, and the children of those objects
   * are the new data of those specific namespaces.
   */
  public Future addData(JsonObject namespacesNewData){
    return client().getCollections().compose(collections -> {
      List<Future<MongoClientBulkWriteResult>> namespaceWriteResults = new ArrayList<>();
      for (String namespace : namespacesNewData.fieldNames()){
        Object namespaceNewData = namespacesNewData.getValue(namespace);
        namespaceWriteResults.add(addJsonItemsToNamespace(namespace, namespaceNewData,
            collection -> collections.contains(collection)));
      }
      return CompositeFuture.all(new ArrayList<>(namespaceWriteResults));
    });
  }

  /**
   * Adds JSON data to a namespace, which may or may not already have a backing MongoDB collection for it. (This will
   * make the collection if it doesn't already exist).
   * TODO refactor
   * @param namespace the name of the namespace to add the new data to
   * @param newData the new data, either a JSON object or JSON array
   * @param collectionExistsChecker function to check if a collection already exists in MongoDB.
   */
  private Future<MongoClientBulkWriteResult> addJsonItemsToNamespace(String namespace, Object newData,
                                                                Function<String, Boolean> collectionExistsChecker){
    String namespaceCollection = collectionPrefix + namespace;
    // If the collection already exists, then just add the new data to it:
    if (collectionExistsChecker.apply(namespaceCollection)){
      return addJsonItemsToCollection(namespaceCollection, newData);
    }
    // If the collection doesn't already exist, then make it before adding the new data:
    else{
      return client().createCollection(namespaceCollection)
          .onFailure(x -> logger.error(x.toString()))
          .compose(discard -> addJsonItemsToCollection(namespaceCollection, newData));
    }
  }

  /**
   * Adds JSON data as documents to an existing MongoDB collection.
   */
  private Future<MongoClientBulkWriteResult> addJsonItemsToCollection(String collection,
                                                                      Object newItems){
    List<BulkOperation> bulkAddNamespaceData = new ArrayList<>();
    if (newItems instanceof JsonArray){
      for (Object item : (JsonArray) newItems){
        if (item instanceof JsonObject){
          bulkAddNamespaceData.add(BulkOperation.createInsert((JsonObject)item));
        }
      }
    }
    else if (newItems instanceof JsonObject){
      // TODO
    }
    return this.client().bulkWrite(collection, bulkAddNamespaceData);
  }
}