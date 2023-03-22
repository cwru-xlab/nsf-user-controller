package nsf.vertx;

import io.vertx.core.Vertx;
import org.apache.log4j.BasicConfigurator;
import org.hyperledger.aries.AriesClient;

public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    // TODO DI everything

    AriesClient ariesClient = AriesClient
        .builder()
        .url("http://localhost:8031")
        //.apiKey("secret") // TODO AUTH (low priority)
        .build();

    vertx.deployVerticle(new ControllerVerticle(ariesClient));
  }
}