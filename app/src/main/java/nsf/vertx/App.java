package nsf.vertx;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.mongo.MongoClient;
import nsf.access.*;
import nsf.controller.ControllerVerticle;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hyperledger.aries.AriesClient;

public class App {

  public static void main(String[] args) {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.INFO);

    DatabindCodec.mapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    Vertx vertx = Vertx.vertx();

    // TODO DI everything

    AriesClient ariesClient = AriesClient
        .builder()
        .url(System.getenv().getOrDefault("AGENT_URL", "http://host.docker.internal:9031"))
        //.apiKey("secret") // TODO AUTH (low priority)
        .build();


    Context context = vertx.getOrCreateContext();
    context.runOnContext(v -> {
      MongoClient mongoClient = MongoDbHelper.getMongoClient(vertx);

      BaseAccessControlService accessControlService =
          AccessControlService.builder().client(mongoClient).collection("access_control").build();

      BaseServProvService servProvService =
          ServProvService.builder().client(mongoClient).collection("service_providers").build();

      BaseDataService dataService =
          DataService.builder().client(mongoClient).build();

      vertx.deployVerticle(new ControllerVerticle(mongoClient, ariesClient, accessControlService, servProvService, dataService));
    });

  }
}