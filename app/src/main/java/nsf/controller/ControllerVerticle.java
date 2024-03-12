package nsf.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import nsf.access.*;
import org.hyperledger.acy_py.generated.model.IndyRequestedCredsRequestedAttr;
import org.hyperledger.aries.AriesClient;
import org.hyperledger.aries.api.connection.ConnectionReceiveInvitationFilter;
import org.hyperledger.aries.api.connection.ConnectionRecord;
import org.hyperledger.aries.api.connection.ReceiveInvitationRequest;
import org.hyperledger.aries.api.out_of_band.InvitationMessage;
import org.hyperledger.aries.api.out_of_band.OOBRecord;
import org.hyperledger.aries.api.out_of_band.ReceiveInvitationFilter;
import org.hyperledger.aries.api.present_proof.PresentationExchangeRecord;
import org.hyperledger.aries.api.present_proof.PresentationRequestCredentials;
import org.hyperledger.aries.api.present_proof.PresentationRequestCredentialsFilter;
import org.hyperledger.aries.api.present_proof.SendPresentationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(ControllerVerticle.class);

  // TODO DI
  private final AriesClient ariesClient;
  private final BaseAccessControlService accessControlService;
  private final BaseServProvService servProvService;
  private final BaseDataService dataService;

  /**
   * Wait between making a connection to an SP and getting their presentation_proof request.
   */
  private final Map<String, RoutingContext> waitingForPresentationReqCtxs = new ConcurrentHashMap<>();

  /**
   * Wait between sending a presentation_proof to an SP and receiving a basic message response, confirming if it was verified or not.
   */
  private final Map<String, RoutingContext> waitingForPresentationResCtxs = new ConcurrentHashMap<>();

  private final Map<String, RoutingContext> waitingForCredentialCtx = new ConcurrentHashMap<>();

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
//    router.route().handler(CorsHandler.create("*")
//        .allowedMethod(HttpMethod.GET)
//        .allowedMethod(HttpMethod.POST)
//        .allowedMethod(HttpMethod.OPTIONS)
//        .allowedMethod(HttpMethod.DELETE)
//        .allowedMethod(HttpMethod.PATCH)
//        .allowedMethod(HttpMethod.PUT)
//        .allowCredentials(true)
//        .allowedHeader("Access-Control-Allow-Headers")
//        .allowedHeader("Authorization")
//        .allowedHeader("Access-Control-Allow-Method")
//        .allowedHeader("Access-Control-Allow-Origin")
//        .allowedHeader("Access-Control-Allow-Credentials")
//        .allowedHeader("Content-Type"));
    router.route().handler(BodyHandler.create());

    router.route().handler(ctx -> {
        ctx.response()
              .putHeader("Access-Control-Allow-Origin", "*")
              .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PATCH, PUT")
              .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
              .putHeader("Access-Control-Allow-Credentials", "true");

        if (ctx.request().method() == HttpMethod.OPTIONS) {
            ctx.response().setStatusCode(200).end();
        } else {
            ctx.next();
        }
    });

    // TODO Refactor split up into multiple handler files.

    router.get("/service-providers").handler(this::listServProvsHandler);
    router.get("/service-providers/:serviceProviderId").handler(this::getServProvDetailHandler);
//    router.get("/relevant-credential").handler(this::checkServiceProviderCredentialRequirements);
    router.post("/service-providers").handler(this::addServiceProviderHandler);
//    router.post("/service-providers/:serviceProviderId/verify").handler(this::verifyCredentialWithServProvider);
    router.post("/verify").handler(this::verifyCredentialWithServProvider);
    router.delete("/service-providers/:serviceProviderId").handler(this::removeServiceProviderHandler);
    router.put("/access/:serviceProviderId").handler(this::setServiceProviderAccessControl);

    router.post("/add-credential").handler(this::addCredential);

    router.post("/push-new-data").handler(new PushDataHandler(ariesClient, accessControlService, servProvService,
        dataService, PushDataTransformer::transformPushableData));

    router.post("/get-data").handler(new GetDataHandler(dataService));

    router.post("/webhook/topic/connections").handler(this::connectionsUpdateHandler);
    router.post("/webhook/topic/issue_credential").handler(this::issueCredentialUpdate);
    router.post("/webhook/topic/present_proof").handler(this::presentProofUpdate);


//    router.post("/webhook/topic/basicmessages").handler(new BasicMessageHandler(dataAccessHandler));

    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
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

  private void addCredential(RoutingContext ctx){
    String invitationUrl = ctx.body().asJsonObject().getString("invitationUrl");
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(invitationUrl);
    List<String> inviteQueryParams = queryStringDecoder.parameters().get("d_m");
    if (inviteQueryParams == null || inviteQueryParams.size() != 1){
      logger.error("Failed to find the single 'd_m' query parameter in invitation URL");
      ctx.response().setStatusCode(400).end();
      return;
    }
    String invitationJsonBase64 = inviteQueryParams.get(0);
    byte[] invitationMsgBytes = Base64.getDecoder().decode(invitationJsonBase64);
    String invitationMsgJsonStr = new String(invitationMsgBytes, StandardCharsets.UTF_8);

    Type type = new TypeToken<ReceiveInvitationRequest>(){}.getType();
    ReceiveInvitationRequest invitationMsg = new Gson().fromJson(invitationMsgJsonStr, type);

    try {
      var connRecordOptional = ariesClient.connectionsReceiveInvitation(invitationMsg,
          ConnectionReceiveInvitationFilter.builder().build());
      var connRecord = connRecordOptional.orElseThrow();
      String connId = connRecord.getConnectionId();

      waitingForCredentialCtx.put(connId, ctx);
    } catch (IOException e) {
      logger.error("Failed to add Service Provider.", e);
      ctx.response().setStatusCode(500).end();
      throw new RuntimeException(e);
    }
  }

  private void verifyCredentialWithServProvider(RoutingContext ctx){
    String presentationExchangeId = ctx.request().getParam("presentationExchangeId");
    String credentialId = ctx.request().getParam("credId"); // frontend has Cred ID from previous request to checkServiceProviderCredentialRequirements.

    Optional<PresentationExchangeRecord> presentationProofResponseOptional = null;
    try {
      presentationProofResponseOptional = ariesClient.presentProofRecordsSendPresentation(
          presentationExchangeId,
          SendPresentationRequest.builder()
              .autoRemove(true)
              .requestedAttributes(
                  Map.of(
                      "issued_referent",
                      SendPresentationRequest.IndyRequestedCredsRequestedAttr.builder()
                          .credId(credentialId)
                          .revealed(true)
                          .build()))
              .build());

      var presentationProofResponse = presentationProofResponseOptional.orElseThrow();
      waitingForPresentationResCtxs.put(presentationExchangeId, ctx);
      // now wait for basic message to see if verified or not...
    } catch (IOException e) {
      logger.error("Failed to send presentation proof.", e);
      ctx.response().setStatusCode(500).send(e.toString());
    }
  }

//  private void checkServiceProviderCredentialRequirements(RoutingContext ctx){
////    String servProvId = ctx.pathParam("serviceProviderId");
//    String presentationExchangeId = ctx.request().getParam("presentationExchangeId");
//
//    try {
//      Optional<String> relevantCredentialId = checkServiceProviderRelevantCredential(presentationExchangeId);
//      ctx.response().setStatusCode(200).send(relevantCredentialId.orElse(""));
//    } catch (IOException e) {
//      logger.error("Failed to get relevant credentials.", e);
//      ctx.response().setStatusCode(500).send(e.toString());
//    }
//  }

  private void getServProvDetailHandler(RoutingContext ctx){
    String servProvId = ctx.pathParam("serviceProviderId");
    getServProvDetail(servProvId)
        .onSuccess(servProvData -> {
          ctx.response().end(servProvData.encode());
        })
        .onFailure(e -> {
          ctx.response().setStatusCode(500).send(e.toString());
        });
  }

  private Future<JsonObject> getServProvDetail(String servProvId) {
    return servProvService.getServProvData(servProvId)
        .compose(servProvData -> {
          Promise<JsonObject> promise = Promise.promise();

          try {
            var relevantCredId = checkServiceProviderRelevantCredential(servProvData
                .getString("presentationExchangeId"));
            servProvData
                .put("relevantCredential", relevantCredId.orElse(""));
          } catch (IOException e) {
            promise.fail("Failed to do relevant credential query");
          }

          promise.complete(servProvData);
          return promise.future();
        });
  }

  private Optional<String> checkServiceProviderRelevantCredential(String presentationExchangeId) throws IOException {
    Optional<List<PresentationRequestCredentials>> relevantCredentialsOptional = Optional.empty();
//    try {
      relevantCredentialsOptional = ariesClient.presentProofRecordsCredentials(
          presentationExchangeId,
          PresentationRequestCredentialsFilter.builder()
              .referent(List.of("issued_referent"))
              .build());
//    } catch (IOException e) {
//      logger.error("Failed to get relevant credentials.", e);
//      ctx.response().setStatusCode(500).send(e.toString());
//    }

    String credentialId = null;
    if (relevantCredentialsOptional.isPresent()){
      var relevantCredentials = relevantCredentialsOptional.orElseThrow();

      if (relevantCredentials.size() > 0){
        var relevantCredential = relevantCredentials.get(0);
        credentialId = relevantCredential.getCredentialInfo().getReferent();
      }
    }

    if (credentialId == null){
      // return no credentials.
      return Optional.empty();
    }
    else{
      return Optional.of(credentialId);
    }
  }

  private void presentProofUpdate(RoutingContext ctx){
    try{
      JsonObject message = ctx.body().asJsonObject();

      logger.info("present_proof updated: " + message.encodePrettily());

      String state = message.getString("state");
      String presentationExchangeId = message.getString("presentation_exchange_id");
      String connId = message.getString("connection_id");

      if (state.equals("request_received")){
        var waitingCtx = waitingForPresentationReqCtxs.get(connId);

        try {
          var presentationRecordOptional = ariesClient.presentProofRecordsGetById(presentationExchangeId);
          var presentationRecord = presentationRecordOptional.orElseThrow();
          String presReqName = presentationRecord.getPresentationRequest().getName();
          JsonObject serverBannerData = new JsonObject(presReqName);

          servProvService.setServProvConnId(connId, presentationExchangeId, serverBannerData)
              .onSuccess((Void) -> {
                logger.info("Added Service Provider mapping.");

                getServProvDetail(connId)
                    .onSuccess(servProvData -> {
                      waitingCtx.response().end(servProvData.encode());
                    })
                    .onFailure(e -> {
                      waitingCtx.response().setStatusCode(500).send(e.toString());
                    });
              })
              .onFailure((Throwable e) -> {
                logger.error("Failed to set ServProv mapping.", e);
                waitingCtx.response().setStatusCode(500).send(e.toString());
              });

        } catch (IOException e) {
          logger.error("Failed to get presentation record.", e);
          waitingCtx.response().setStatusCode(500).send(e.toString());
        }
      }

      ctx.response().setStatusCode(200).end();
    }
    catch(Exception e){
      ctx.response().setStatusCode(500).end();
    }
  }


  private void issueCredentialUpdate(RoutingContext ctx){
    try{
      JsonObject message = ctx.body().asJsonObject();

      // Docs: https://aca-py.org/latest/features/AdminAPI/#pairwise-connection-record-updated-connections
      String userConnectionId = message.getString("connection_id");
      String state = message.getString("state");
      String credentialId = message.getString("credential_id");

      logger.info("issue_credential updated: " + userConnectionId + ", " + state + ", " + credentialId + " - " + message.encodePrettily());

      if (state.equals("deleted")){
        logger.info("issue_credential deleted, assuming added the credential properly.");
        waitingForCredentialCtx.get(userConnectionId).end(credentialId);
        // ^ TODO add timeout timer to return 400 if no response.
      }

      ctx.response().setStatusCode(200).end();
    }
    catch(Exception e){
      ctx.response().setStatusCode(500).end();
    }
  }

  private void connectionsUpdateHandler(RoutingContext ctx){
      try{
          JsonObject message = ctx.body().asJsonObject();

          // Docs: https://aca-py.org/latest/features/AdminAPI/#pairwise-connection-record-updated-connections
          String userConnectionId = message.getString("connection_id");
          String state = message.getString("state");
          String invitationKey = message.getString("invitation_key");

          logger.info("connection updated: " + userConnectionId + ", " + state + " - " + message.encodePrettily());

          ctx.response().setStatusCode(200).end();
      }
      catch(Exception e){
          ctx.response().setStatusCode(500).end();
      }
  }

  public void listServProvsHandler(RoutingContext ctx){
      servProvService.listServProvs().onSuccess((List<JsonObject> servProvs) -> {
          var servProvsArray = new JsonArray(servProvs);
          ctx.response()
              .setStatusCode(200)
              .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
              .end(servProvsArray.encodePrettily());
      });
  }

  /**
   * Handles post request for establishing a connection to a service provider given an invitation message JSON from
   * that service provider in the post body. This tells the ACA-Py agent that we have "received" the invitation
   * message, and progresses the state of the connection.
   */
  private void addServiceProviderHandler(RoutingContext ctx){
    // Deserialize Vertx body via Gson (since ACA-Py wrapper takes Gson-serializable InvitationMessage):
    String invitationMsgUrl = ctx.request().getFormAttribute("invitationUrl");
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(invitationMsgUrl);
    List<String> oobQueryParams = queryStringDecoder.parameters().get("oob");
    if (oobQueryParams == null || oobQueryParams.size() != 1){
      logger.error("Failed to find the single 'oob' query parameter in invitation URL");
      ctx.response().setStatusCode(400).end();
      return;
    }
    String invitationJsonBase64 = oobQueryParams.get(0);
    byte[] invitationMsgBytes = Base64.getDecoder().decode(invitationJsonBase64);
    String invitationMsgJsonStr = new String(invitationMsgBytes, StandardCharsets.UTF_8);

    Type type = new TypeToken<InvitationMessage<Object>>(){}.getType();
    InvitationMessage<Object> invitationMsg = new Gson().fromJson(invitationMsgJsonStr, type);

    try {
      Optional<OOBRecord> oobRecordOptional = ariesClient.outOfBandReceiveInvitation(invitationMsg,
          ReceiveInvitationFilter.builder().autoAccept(true).build());
      var oobRecord = oobRecordOptional.orElseThrow();
      String connId = String.valueOf(oobRecord.getConnectionId());

      waitingForPresentationReqCtxs.put(connId, ctx);
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
                        ariesClient.connectionsRemove(connId); // TODO doesnt seem to remove connection on service provider side.
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