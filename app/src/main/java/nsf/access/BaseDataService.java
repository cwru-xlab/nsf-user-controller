package nsf.access;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Value.Immutable
public abstract class BaseDataService {
  private static final Logger logger = LoggerFactory.getLogger(BaseDataService.class);
  public static String namespaceToCollectionName(String namespace){
    return "data." + namespace;
  }
  public abstract MongoClient client();

  /**
   * Reads previously-stored data of a specific namespace.
   */
  public Future<List<JsonObject>> readNamespaceData(String namespace){
    JsonObject query = new JsonObject();
    return client().find(namespaceToCollectionName(namespace), query);
  }

  /**
   * Saves new JSON namespaces in the database. This data must follow the schema where the first layer objects
   * (immediate children of the given/root new data object) are for the different namespaces being updated.
   */
  public Future<CompositeFuture> saveNewNamespaces(JsonObject newNamespacesData){
    return client().getCollections().compose(collections -> {
      List<Future<Void>> namespaceWriteResults = new ArrayList<>();
      for (String namespaceName : newNamespacesData.fieldNames()){
        JsonObject namespaceValue = newNamespacesData.getJsonObject(namespaceName);
        namespaceWriteResults.add(addJsonObjToNamespace(namespaceName, namespaceValue,
            collections::contains));
      }
      return CompositeFuture.all(new ArrayList<>(namespaceWriteResults));
    });
  }

  /**
   * Adds JSON data to a namespace, which may or may not already have a backing MongoDB collection for it. (This will
   * make the collection if it doesn't already exist).
   */
  private Future<Void> addJsonObjToNamespace(String namespaceName, JsonObject newData,
                                       Function<String, Boolean> collectionExistsChecker){
    String namespaceCollection = namespaceToCollectionName(namespaceName);
    // If the collection already exists, then just add the new data to it:
    if (collectionExistsChecker.apply(namespaceCollection)){
      return addJsonObjToCollection(namespaceCollection, newData);
    }
    // If the collection doesn't already exist, then make it before adding the new data:
    else{
      return client().createCollection(namespaceCollection)
          .onFailure(x -> logger.error(x.toString()))
          .compose(discard -> addJsonObjToCollection(namespaceCollection, newData));
    }
  }

  /**
   * Adds JSON data as documents to an existing MongoDB collection, forgetting about the document's ID.
   */
  private Future<Void> addJsonObjToCollection(String collection, JsonObject newJsonObjDoc){
    return this.client().save(collection, newJsonObjDoc)
        // Save Future completes with the document's ID, but we don't care about it:
        .compose(discardedDocId -> Future.succeededFuture());
  }
}