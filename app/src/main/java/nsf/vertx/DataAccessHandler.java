package nsf.vertx;

import io.vertx.ext.web.RoutingContext;
import nsf.controller.DataAccessRequest;

/**
 * Duct tape to see stuff working...
 * TODO REFACTOR
 */
public class DataAccessHandler {
    public DataAccessHandler(){

    }

    public void processDataAccess(RoutingContext ctx, DataAccessRequest dataAccessRequest){
        System.out.println("Received DataAccessRequest: " + dataAccessRequest.getOperation() + " - " + dataAccessRequest.getResourceUri());
        ctx.response().setStatusCode(200).end();
    }
}
