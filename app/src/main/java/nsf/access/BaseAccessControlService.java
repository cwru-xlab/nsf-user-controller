package nsf.access;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import org.immutables.value.Value;

@Value.Immutable
public abstract class BaseAccessControlService {

    public abstract String collection();
    public abstract MongoClient client();

    public Future<String> createPolicyById(Policy newPolicy) {
        JsonObject document = new JsonObject(Json.encode(newPolicy));
        return this.client().save(this.collection(), document);
    }

    /**
     * Reads policy by Service Provider ID.
     * @param id the Service Provider ID
     * @return the Future Policy
     */
    public Future<Policy> readPolicyById(String id) {
        JsonObject query = new JsonObject()
                .put("_id", id);
        return client().findOne(collection(), query, null).compose(json -> {
            Promise<Policy> promise = Promise.promise();
            promise.complete(json.mapTo(Policy.class));
            return promise.future();
        });
    }

    public Future<MongoClientUpdateResult> updatePolicyById(String id, Policy newPolicy) {
        JsonObject document = new JsonObject(Json.encode(newPolicy));
        UpdateOptions options = new UpdateOptions().setMulti(true);
        JsonObject query = new JsonObject()
                .put("_id", id);
        // TODO: finish this
        return this.client().updateCollectionWithOptions(this.collection(), query, document, options);
    }

    public Future<MongoClientDeleteResult> deletePolicyById(String id) {
        JsonObject query = new JsonObject()
                .put("_id", id);
        // TODO: finish this
        return client().removeDocument(collection(), query);
    }

}
