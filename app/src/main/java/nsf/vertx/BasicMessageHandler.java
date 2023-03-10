package nsf.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import nsf.controller.DataAccessRequest;

public class BasicMessageHandler implements Handler<RoutingContext> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DataAccessHandler userDataService;

    public BasicMessageHandler(DataAccessHandler userDataService){
        this.userDataService = userDataService;
    }

    @Override
    public void handle(RoutingContext ctx) {
        io.vertx.core.json.JsonObject bodyJson = ctx.body().asJsonObject();
        String messageContent = bodyJson.getString("content");
        DataAccessRequest dataAccessRequest;
        try {
            dataAccessRequest = mapper.readValue(messageContent, DataAccessRequest.class);
        } catch (Exception e) {
            // TODO LOGGING
            System.out.println("Failed to parse BasicMessage JSON");
            ctx.response().setStatusCode(400).end("Invalid JSON Schema");
            return;
        }

        // TODO can probably route this cleanly through Vertx
        userDataService.processDataAccess(ctx, dataAccessRequest);
    }
}
