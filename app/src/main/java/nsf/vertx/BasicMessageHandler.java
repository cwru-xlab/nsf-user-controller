package nsf.vertx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import nsf.controller.DataAccessHandler;

public class BasicMessageHandler implements Handler<RoutingContext> {
  private final ObjectMapper mapper = new ObjectMapper();
  private final DataAccessHandler dataAccessHandler;

  public BasicMessageHandler(DataAccessHandler dataAccessHandler){
    this.dataAccessHandler = dataAccessHandler;
  }

  @Override
  public void handle(RoutingContext ctx) {
    String connectionId;
    String messageId;
    String messageType;
    JsonNode dataJson;
    try {
      io.vertx.core.json.JsonObject bodyJson = ctx.body().asJsonObject();
      connectionId = bodyJson.getString("connection_id");
      messageId = bodyJson.getString("message_id");
      String messageContent = bodyJson.getString("content");
      JsonNode contentJson = mapper.readTree(messageContent);
      messageType = contentJson.get("messageType").asText();
      dataJson = contentJson.get("data");
    } catch (Exception e) {
      // TODO LOGGING
      System.out.println("Failed to parse BasicMessage JSON: " + e);
      ctx.response().setStatusCode(400).end();
      return;
    }

    switch (messageType){
      case "DATA_ACCESS_REQUEST":
        dataAccessHandler.handleDataAccess(ctx.response(), dataJson, connectionId, messageId);
        break;
      case "DATA_ACCESS_RESPONSE":
        // TODO REMOVE THIS CASE - RESPONSE WILL BE ON SERVPROV SIDE
        //  - (I just put it here to listen to test messages to confirm that it works)

        System.out.println("Fetched Data:\n\n" + dataJson.get("requestedData") + "\n\n(This would have been sent to " +
            "the service provider, but we haven't made the service provider controller yet, since we want to base it off " +
            "of this user controller which we are still finishing)");
        break;
      default:
        System.out.println("Unhandled message type: " + messageType);
        ctx.response().setStatusCode(400).end();
        break;
    }
  }
}
