package nsf.vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import nsf.access.AccessControlService;
import nsf.access.BaseAccessControlService;
import nsf.access.MongoDbHelper;
import nsf.controller.ControllerVerticle;
import org.apache.log4j.BasicConfigurator;
import org.hyperledger.aries.AriesClient;

public class App {

  public static void main(String[] args) {
    BasicConfigurator.configure();

    Vertx vertx = Vertx.vertx();

    // TODO DI everything

    AriesClient ariesClient = AriesClient
        .builder()
        .url("http://localhost:8031")
        //.apiKey("secret") // TODO AUTH (low priority)
        .build();

    MongoClient mongoClient = MongoDbHelper.getMongoClient(vertx);
    BaseAccessControlService accessControlService =
        AccessControlService.builder().client(mongoClient).collection("access_control").build();
    vertx.deployVerticle(new ControllerVerticle(ariesClient, accessControlService));
  }
}