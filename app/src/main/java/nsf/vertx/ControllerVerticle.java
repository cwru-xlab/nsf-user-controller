package nsf.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import nsf.controller.DataAccessHandler;

public class ControllerVerticle extends AbstractVerticle {
  private final DataAccessHandler dataAccessHandler;

  public ControllerVerticle(DataAccessHandler dataAccessHandler) {
    this.dataAccessHandler = dataAccessHandler;
  }

  @Override
  public void start(Promise<Void> promise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.post("/webhook/topic/basicmessages")
        // TODO DI
        .handler(new BasicMessageHandler(dataAccessHandler));

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(8080)
        // TODO CONFIG
        //.listen(config().getInteger("http.port", 8080));
        .onSuccess(server -> {
          // TODO LOGGING
          System.out.println("server running! (Should be listening to webhooks from the agent)");
          promise.complete();
        })
        .onFailure(promise::fail);
  }
}
