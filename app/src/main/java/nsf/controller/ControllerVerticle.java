package nsf.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import nsf.access.*;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.out_of_band.InvitationMessage;
import org.hyperledger.aries.api.out_of_band.ReceiveInvitationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ControllerVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(ControllerVerticle.class);

  // TODO DI
  private final AriesClient ariesClient;
  private final BaseAccessControlService accessControlService;
  private final BaseServProvService servProvService;
  private final BaseDataService dataService;

  public ControllerVerticle(AriesClient ariesClient, BaseAccessControlService accessControlService,
                            BaseServProvService servProvService, BaseDataService dataService) {
    this.ariesClient = ariesClient;
    this.accessControlService = accessControlService;
    this.servProvService = servProvService;
    this.dataService = dataService;
  }

  @Override
  public void start(Promise<Void> promise) {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    // TODO Refactor split up into multiple handler files.

    // NOTE: normally you make separate Handler classes for the handlers functions, but these service provider ones
    // are so simple that we should probably just make them actual functions, maybe grouped in their own controller
    // class if we want.
    router.post("/service-providers/:serviceProviderId").handler(this::addServiceProviderHandler);
    router.delete("/service-providers/:serviceProviderId").handler(this::removeServiceProviderHandler);

    router.put("/access/:serviceProviderId").handler(this::setServiceProviderAccessControl);

    router.post("/push-new-data").handler(new PushDataHandler(ariesClient, accessControlService, servProvService,
        dataService, PushDataTransformer::transformPushableData));

    router.post("/get-data").handler(new GetDataHandler(dataService));

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
   * that service provider in the post body. This tells the ACA-Py agent that we have "received" the invitation
   * message, and progresses the state of the connection.
   */
  private void addServiceProviderHandler(RoutingContext ctx){
    // Deserialize Vertx body via Gson (since ACA-Py wrapper takes Gson-serializable InvitationMessage):
    String servProvId = ctx.pathParam("serviceProviderId");
    String invitationMsgUrl = ctx.body().asString();
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(invitationMsgUrl);
    List<String> oobQueryParams = queryStringDecoder.parameters().get("oob");
    if (oobQueryParams == null & oobQueryParams.size() != 0){
      logger.error("Failed to find the single 'oob' query parameter in invitation URL");
      return;
    }
    String invitationJsonBase64 = oobQueryParams.get(0);
    byte[] invitationMsgBytes = Base64.getDecoder().decode(invitationJsonBase64);
    String invitationMsgJsonStr = new String(invitationMsgBytes, StandardCharsets.UTF_8);

    Type type = new TypeToken<InvitationMessage<Object>>(){}.getType();
    InvitationMessage<Object> invitationMsg = new Gson().fromJson(invitationMsgJsonStr, type);

    try {
      Optional<ConnectionRecord> returnedAcapyConnection = ariesClient.outOfBandReceiveInvitation(invitationMsg,
          ReceiveInvitationFilter.builder().autoAccept(true).build());
      ConnectionRecord acapyConnection = returnedAcapyConnection.orElseThrow(() -> new IOException("Did not get an " +
          "ACA-Py connection."));

      servProvService.setServProvConnId(servProvId, acapyConnection.getConnectionId())
          .onSuccess((Void) -> {
            logger.info("Added Service Provider ID to connection ID mapping.");
            ctx.response().setStatusCode(200).end();
          })
          .onFailure((Throwable e) -> {
            logger.error("Failed to set policy for ServProv.", e);
            ctx.response().setStatusCode(500).send(e.toString());
          });
    } catch (IOException e) {
      logger.error("Failed to add Service Provider.", e);
      ctx.response().setStatusCode(500).end();
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles delete request for removing a connection to a service provider, thereby removing the service provider's
   * access.
   */
  private void removeServiceProviderHandler(RoutingContext ctx){
    String servProvId = ctx.pathParam("serviceProviderId");

    // TODO REFACTOR 💀
    // Delete the service provider's access control policy, then delete the service provider object:
    // NOTE on onFailure: Even if these documents don't exist in the database, they will still succeed as futures
    //  and simply not do anything. So if a future fails here then it is unexpected.
    accessControlService.deletePolicyById(servProvId)
        .onSuccess((MongoClientDeleteResult deletePolicyResult) -> {
          servProvService.getServProvConnId(servProvId)
              .onSuccess((String connId) -> {
                // Important order: the policy must be deleted before the service provider object to avoid an orphaned
                // policy on failure.
                servProvService.deleteServProvConnMapping(servProvId)
                    .onSuccess((MongoClientDeleteResult deleteServProvObjResult) -> {
                      try {
                        ariesClient.connectionsRemove(connId);
                      } catch (IOException e) {
                        logger.error("Failed to remove Service Provider connection.", e);
                        ctx.response().setStatusCode(500).end();
                        throw new RuntimeException(e);
                      }
                      logger.info("Deleted Service Provider.");
                      ctx.response().setStatusCode(200).end();
                    })
                    .onFailure((Throwable e) -> {
                      logger.error("Failed to delete Service Provider object.", e);
                      ctx.response().setStatusCode(500).send(e.toString());
                    });
              })
              .onFailure((Throwable e) -> {
                logger.error("Failed to get Service Provider object.", e);
                ctx.response().setStatusCode(500).send(e.toString());
              });
        })
        .onFailure((Throwable e) -> {
          logger.error("Failed to delete Service Provider access control policy.", e);
          ctx.response().setStatusCode(500).send(e.toString());
        });
  }

  /**
   * REMARK: Currently access control is quite limited and does not allow fine-grain per-resource access control, as
   * the access rules of a single service provider are currently defined by independent
   */
  private void setServiceProviderAccessControl(RoutingContext ctx){
    String serviceProviderId = ctx.pathParam("serviceProviderId");

    JsonObject product = ctx.body().asJsonObject();
    PolicyModel policyModel = product.mapTo(PolicyModel.class);

    servProvService.getServProv(serviceProviderId)
        .onSuccess((Optional<JsonObject> nullableJsonObj) -> {
          if (nullableJsonObj.isPresent()){
            accessControlService.createPolicyById(policyModel.toEntity(serviceProviderId))
                .onSuccess((String nullableResponse) -> {
                  logger.info("Updated policy for ServProv: " + serviceProviderId);
                  ctx.response().setStatusCode(200).end();
                })
                .onFailure((Throwable e) -> {
                  logger.error("Failed to set policy for ServProv.", e);
                  ctx.response().setStatusCode(500).send(e.toString());
                });
          }
          else{
            ctx.response().setStatusCode(400).send("Service Provider not found. Make sure you have added the Service " +
                "Provider.");
          }
        })
        .onFailure((Throwable e) ->{
          logger.error("Failed to set access control policy.", e);
          ctx.response().setStatusCode(500).send(e.toString());
        });
  }
}