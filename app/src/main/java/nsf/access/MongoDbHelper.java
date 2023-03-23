package nsf.access;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoDbHelper {

    public static MongoClient getMongoClient(Vertx vertx) {
        JsonObject config = Vertx.currentContext().config();
        // TODO: add config file
        String uri = config.getString("mongo_uri");
        if (uri == null) {
            uri = "mongodb://localhost:27017";
        }
        String db = config.getString("mongo_db");
        if (db == null) {
            db = "nsf";
        }

        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", uri)
                .put("db_name", db);

        MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);
        return mongoClient;
    }
}
