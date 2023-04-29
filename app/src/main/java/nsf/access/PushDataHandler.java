package nsf.access;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PushDataHandler implements Handler<RoutingContext> {
  private static final Logger logger = LoggerFactory.getLogger(PushDataHandler.class);
  private final AriesClient ariesClient;
  private final BaseAccessControlService accessControlService;
  private final BaseServProvService servProvService;
  private final BaseDataService dataService;
  private final Function<JsonObject,JsonObject> pushableDataTransformer;

  public PushDataHandler(AriesClient ariesClient, BaseAccessControlService accessControlService,
                         BaseServProvService servProvService, BaseDataService dataService, Function<JsonObject,
                         JsonObject> pushableDataTransformer){
    this.ariesClient = ariesClient;
    this.accessControlService = accessControlService;
    this.servProvService = servProvService;
    this.dataService = dataService;
    this.pushableDataTransformer = pushableDataTransformer;
  }

  @Override
  public void handle(RoutingContext ctx) {
    JsonObject newDataJson = ctx.body().asJsonObject();

    dataService.addData(newDataJson).onSuccess(discard -> {
      // TODO refactor
      accessControlService.readAllSubscribePolicies()
          .onSuccess(policies -> {
            // Transform the incoming data into the data that will actually be pushed:
            JsonObject finalPushData = pushableDataTransformer.apply(newDataJson);
            // Push to Service Providers:
            List<Future<String>> pushFutures = pushToServProvs(finalPushData, policies);

            // Wait till pushed to all Service Providers, then respond with the respective result messages:
            CompositeFuture.all(new ArrayList<>(pushFutures))
                .onSuccess((compositeFuture) -> {
                  // Map each result message to a line on the response body:
                  List<String> resultMsgs = pushFutures.stream().map(Future::result).collect(Collectors.toList());
                  String combinedResultMsgs = String.join("\n", resultMsgs);

                  logger.info("Pushed new data ({} pushes):\n{}", resultMsgs.size(), combinedResultMsgs);

                  // Respond:
                  if (resultMsgs.size() > 0){
                    ctx.response().setStatusCode(200).send("Pushed new data:\n " + combinedResultMsgs);
                  }
                  else{
                    ctx.response().setStatusCode(200).send("Pushed no data, as no Service Providers were subscribed to " +
                        "any of the pushed resources.");
                  }
                })
                .onFailure((Throwable e) -> {
                  logger.error("Failed to push new data.", e);
                  ctx.response().setStatusCode(500).send("Failed to push new data because the following exception " +
                      "occurred during a push to a Service Provider: " + e);
                });
          })
          .onFailure((Throwable e) -> {
            logger.error("Failed to read access control subscribe policies.", e);
            ctx.response().setStatusCode(500).send("Failed to push because failed to read access control " +
                "subscribe policies: " + e);
          });
    });
  }

  /**
   * Pushes the given JSON resources (which include all the data in those resources) according to the given policies.
   * @param pushableJsonResources a JSON object where each child key-value represents a resource and that resource's
   *                              data respectively.
   * @param policies the considered policies that say which Service Providers are subscribed to which resources.
   * @return a list of futures where each sends a message to a service provider (which "pushes" the new data to them).
   */
  private List<Future<String>> pushToServProvs(JsonObject pushableJsonResources, List<Policy> policies){
    List<Future<String>> pushFutures = new ArrayList<>();
    for (Policy servProvPolicy : policies){
      String servProvId = servProvPolicy.serviceProviderId();

      // Get the resources that this Service Provider is subscribed to:
      List<String> subscribedResources = servProvPolicy.resources().stream()
          .filter(pushableJsonResources::containsKey).collect(Collectors.toList());

      // Do nothing if this Service Provider is not subscribed to any of the given resources:
      if (subscribedResources.size() == 0)
        continue;

      // Turn the resources into a JSON data body:
      JsonObject servProvPushData = new JsonObject();
      for (String resource : subscribedResources)
        servProvPushData.put(resource, pushableJsonResources.getJsonArray(resource));

      // Future push the JSON data and give a result message:
      Future<String> sendMessageFuture = pushDataToServProv(servProvPushData, servProvId)
          .compose(connId -> {
            Promise<String> promise = Promise.promise();
            promise.complete("Pushed [" + String.join(", ", subscribedResources) + "] to " + servProvId);
            return promise.future();
          });
      pushFutures.add(sendMessageFuture);
    }
    return pushFutures;
  }

  /**
   * Sends or "pushes" given JSON data to a given Service Provider.
   */
  private Future<String> pushDataToServProv(JsonObject jsonData, String servProvId){
    // Get the Service Provider's ACA-Py connection ID, and then send them the Basic Message:
    return servProvService.getServProvConnId(servProvId)
        .onSuccess(connId -> {
          // Build the ACA-Py Basic Message to send:
          SendMessage basicMessageResponse = SendMessage.builder()
              .content(jsonData.toString())
              .build();

          // Send the Basic Message via ACA-Py client:
          try {
            ariesClient.connectionsSendMessage(connId, basicMessageResponse);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .onFailure((Throwable e) -> {
          // Don't have to handle error here, as it bubbles up in the returned future.
          logger.error("Failed to get service provider info.", e);
        });
  }
}
