package nsf.vertx;

import io.vertx.core.Vertx;
import nsf.controller.DataAccessHandler;
import org.hyperledger.aries.AriesClient;

public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    // TODO DI everything
    DataService dataService = new DataService();
    vertx.deployVerticle(dataService);

    AriesClient ariesClient = AriesClient
        .builder()
        .url("http://localhost:8021")
        //.apiKey("secret")
        .build();

    DataAccessHandler dataAccessHandler = new DataAccessHandler(dataService, ariesClient);

    vertx.deployVerticle(new ControllerVerticle(dataAccessHandler));
  }
}
