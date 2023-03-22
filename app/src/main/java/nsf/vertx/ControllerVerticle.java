package nsf.vertx;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.out_of_band.InvitationMessage;
import org.hyperledger.aries.api.out_of_band.ReceiveInvitationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class ControllerVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(ControllerVerticle.class);

  // TODO DI
  private final AriesClient ariesClient;

  public ControllerVerticle(AriesClient ariesClient) {
    this.ariesClient = ariesClient;
  }

  @Override
  public void start(Promise<Void> promise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // NOTE: normally you make separate Handler classes for the handlers functions, but these service provider ones
    // are so simple that we should probably just make them actual functions, maybe grouped in their own controller
    // class if we want.
    router.post("/service-providers").handler(this::acceptServiceProviderHandler);
    router.delete("/service-providers").handler(this::removeServiceProviderHandler);

    // TODO Only need to receive msgs on the user agent for the returning score in NSF use case, not Progressive.
//    router.post("/webhook/topic/basicmessages").handler(new BasicMessageHandler(dataAccessHandler));

//    int port = config().getInteger("http.port", 8080); // TODO CONFIG
    int port = 8080;
    vertx.createHttpServer()
        .requestHandler(router)
        .listen(port)
        .onSuccess(server -> {
          // TODO LOGGING
          logger.info(String.format("server running! (Should be listening at port %s)", port));
          promise.complete();
        })
        .onFailure(promise::fail);
  }

  /**
   * Handles post request for establishing a connection to a service provider given an invitation message JSON from
   * that service provider in the post body.
   */
  private void acceptServiceProviderHandler(RoutingContext ctx){
    // Deserialize Vertx body via Gson (since ACA-Py wrapper takes Gson-serializable InvitationMessage):
    String invitationMsgStr = ctx.body().asString();
    Type type = new TypeToken<InvitationMessage<Object>>(){}.getType();
    InvitationMessage<Object> invitationMsg = new Gson().fromJson(invitationMsgStr, type);

    try {
      ariesClient.outOfBandReceiveInvitation(invitationMsg,
          ReceiveInvitationFilter.builder().build());
      ctx.response().setStatusCode(200).end();
    } catch (IOException e) {
      ctx.response().setStatusCode(500).end();
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles delete request for removing a connection to a service provider, thereby removing the service provider's
   * access.
   */
  private void removeServiceProviderHandler(RoutingContext ctx){
    // TODO need metastore to store service providers in (as a map from servprov IDs to conn IDs).
    ctx.response().setStatusCode(501).end();
  }
}