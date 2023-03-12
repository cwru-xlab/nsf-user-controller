package nsf.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import nsf.pda.data.PdaData;
import nsf.vertx.DataService;
import org.hyperledger.acy_py.generated.model.SendMessage;
import org.hyperledger.aries.AriesClient;

import java.io.IOException;

/**
 * Duct tape to see stuff working...
 * TODO REFACTOR
 */
public class DataAccessHandler {
  private final ObjectMapper mapper = new ObjectMapper();
  private final DataService dataService;
  private AriesClient ariesClient;

  public DataAccessHandler(DataService dataService, AriesClient ariesClient){
    this.dataService = dataService;
    this.ariesClient = ariesClient;
  }

  public void handleDataAccess(HttpServerResponse response, JsonNode requestDataJson, String connectionId,
                               String messageId){
    DataAccessRequest dataAccessRequest;
    try {
      dataAccessRequest = mapper.treeToValue(requestDataJson, DataAccessRequest.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // TODO some switch for if this is a PDA request or some other data driver - this logic would have to be done
    //  through looking up the resource datastore type from the metadata db.

    Future<PdaData> accessOperation = dataService.accessFromPda(dataAccessRequest);

    System.out.println("Processing DataAccessRequest... (" + dataAccessRequest.getOperation() + " - " + dataAccessRequest.getResourceIdentifier() + ")");
    accessOperation
        .onSuccess(fetchedData -> sendDataAccessResponse(fetchedData, response, connectionId, messageId))
        .onFailure(err -> {
          System.out.println("DataAccess failed: " + err);
          if (err instanceof DataAccessException){
            response.setStatusCode(400).end();
          }
          else{
            response.setStatusCode(500).end();
          }
        });
  }

  private void sendDataAccessResponse(PdaData fetchedData, HttpServerResponse response, String connectionId,
                                      String messageId){
    try {
      JsonObject rootBasicMessageNode = new JsonObject()
          .put("messageType", "DATA_ACCESS_RESPONSE");

      JsonObject dataNode = new JsonObject()
          .put("requestMessageId", messageId)
          .put("requestedData", fetchedData.get());
      rootBasicMessageNode.put("data", dataNode);

      String stringifiedContent = rootBasicMessageNode.toString();
      SendMessage basicMessageResponse = SendMessage.builder()
          .content(stringifiedContent)
          .build();

      //ariesClient.connectionsSendMessage(connectionId, basicMessageResponse);
      ariesClient.connectionsSendMessage("7a55d83d-4980-46db-bf34-4b311e7b5181", basicMessageResponse);

      response.setStatusCode(200).end();
    } catch (JsonProcessingException e) {
      System.out.println("DataAccess response failed: " + e);
      response.setStatusCode(500).end();
    } catch (IOException e) {
      System.out.println("Sending response message failed: " + e);
      response.setStatusCode(500).end();
    }
  }
}
