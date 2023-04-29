package nsf.access;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GetDataHandler implements Handler<RoutingContext> {
  private static final Logger logger = LoggerFactory.getLogger(GetDataHandler.class);
  private BaseDataService dataService;

  public GetDataHandler(BaseDataService dataService){
    this.dataService = dataService;
  }

  @Override
  public void handle(RoutingContext ctx) {
    JsonObject responseData = new JsonObject();
    List<Future> readFutures = new ArrayList<>();

    JsonObject requestedGetNamespaces;
    try{
      requestedGetNamespaces = ctx.body().asJsonObject();
    }
    catch (Exception e){
      ctx.response().setStatusCode(400).end();
      throw e;
    }
    if (requestedGetNamespaces == null){
      ctx.response().setStatusCode(400).end();
      return;
    }

    for (String namespace : requestedGetNamespaces.fieldNames()){
      readFutures.add(dataService.readNamespaceData(namespace).onSuccess(data -> {
        JsonArray namespaceDataArray = new JsonArray(data);
        responseData.put(namespace, namespaceDataArray);
      }));
    }

    CompositeFuture.all(readFutures).onSuccess(discard -> {
      ctx.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
          .end(responseData.encodePrettily());
    });
  }
}
