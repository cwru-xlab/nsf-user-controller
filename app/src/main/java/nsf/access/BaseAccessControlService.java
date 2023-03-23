package nsf.access;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@Value.Immutable
public abstract class BaseAccessControlService {

    public abstract String collection();
    public abstract MongoClient client();

    public Future<String> createServiceProviderById(ServiceProvider newServiceProvider) {
        JsonObject document = new JsonObject(Json.encode(newServiceProvider));
        return this.client().save(this.collection(), document);
    }

    public Future<ServiceProvider> readServiceProviderById(String id) {
        JsonObject query = new JsonObject()
                .put("_id", id);
        return client().find(collection(), query).compose(res -> {
            ArrayList<ServiceProvider> serviceProviderList = new ArrayList<>();
            for (JsonObject json : res) {
                System.out.println(json.encode());
                serviceProviderList.add(Json.decodeValue(json.encode(), ServiceProvider.class));
            }
            // TODO: I can't figure out how to do a Future in vert.x
            return null;
        });
    }

    public Future<MongoClientUpdateResult> updateServiceProviderById(String id, ServiceProvider newServiceProvider) {
        JsonObject document = new JsonObject(Json.encode(newServiceProvider));
        UpdateOptions options = new UpdateOptions().setMulti(true);
        JsonObject query = new JsonObject()
                .put("_id", id);
        // TODO: finish this
        return this.client().updateCollectionWithOptions(this.collection(), query, document, options);
    }

    public Future<MongoClientDeleteResult> deleteServiceProviderById(String id) {
        JsonObject query = new JsonObject()
                .put("_id", id);
        // TODO: finish this
        return client().removeDocument(collection(), query);
    }

}
