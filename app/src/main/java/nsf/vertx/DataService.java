package nsf.vertx;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import nsf.controller.DataAccessRequest;
import nsf.controller.DataResourceIdentifier;
import nsf.pda.PdaException;
import nsf.pda.auth.AccessToken;
import nsf.pda.data.PdaData;

/**
 * Placeholder experimenting with PDA integration
 * TODO use {@link TokenService}
 */
public class DataService extends AbstractVerticle {
  private final ObjectMapper mapper = new ObjectMapper();
  private AccessToken token;

  private WebClient client;

  @Override
  public void start(Promise<Void> promise) {
    // TODO verticle messages
    client = WebClient.create(vertx);
    promise.complete();
  }

  public Future<PdaData> accessFromPda(DataAccessRequest dataAccessRequest) {
    client = WebClient.create(vertx);
    switch (dataAccessRequest.getOperation()){
      case READ:
        return getFromPda(dataAccessRequest.getResourceIdentifier());
      default:
        return Future.failedFuture("Access operation not implemented.");
    }
  }

  private Future<PdaData> getFromPda(DataResourceIdentifier resourceIdentifier) {
    Promise<PdaData> promise = Promise.promise();

    // TODO STORE USER DOMAIN IN DATASTORE METADATA DB
    String userPdaDomainPlaceholder = "bbm38wyv.hubat.net";
    client
        .get(userPdaDomainPlaceholder, "/api/v2.6/data/test-namespace/test-endpoint")
        .as(BodyCodec.jsonArray())
        .putHeader("host", userPdaDomainPlaceholder)
        .putHeader("content-type", "application/json")
        .putHeader("x-auth-token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxLUtGcXRkQ0FvMHhtMzJaZlVMRFB5amVJQXJIaTVYWDVCNHhpaEVQd1RwU3dPK2RycVwvU3dVdkNTbzBIaEZJcll0bEJPMFFHN045VWI4QlhUMlRaOWpxNGZQeVNuQmRKODJTRGg0RFE9PSIsInJlc291cmNlIjoiYmJtMzh3eXYuaHViYXQubmV0IiwiYWNjZXNzU2NvcGUiOiJvd25lciIsImlzcyI6ImJibTM4d3l2Lmh1YmF0Lm5ldCIsImV4cCI6MTY4MTEyMzE5NywiaWF0IjoxNjc4NTMxMTk3LCJqdGkiOiIwOTM4YzZhMWVmZGVmOTk3NDJjNDU1Yzc3ZTYwZTAxY2RlOWEzMDlkMGM5NGEzYTg4Njg2MGMxOTg5ZjJhNzQ5M2Q5ZDg2NjU1Mzg4MTI3NjkyOTg3NTQ1OWZjYjE1MWRlMGJlZjEwN2UyYzE1Yjg3OGNkNmY5NDE3NmM3YzllZWM1YmRmMjRmYTVlM2FhMTUxZTdlNDhkMTIxYmFmM2Y3MmM3OGY3ZGMzZDMyOGI0MmI5NzM1OGJiZGRhNjlkZTJkM2I1YWE4Zjg4MzAwZWFhY2NiNmFjMmYyOTNkNTYwZTc5MzZhNjRiZmM2MzhkNWM5N2RjNDIyYTYxZWY0MTVhIn0.eQcDgglU_hrOlOm1SYthZLCxZkAz2EbAbmHFAu4ijkibAVd8yH90LNVt-IJqyAwDXz_0QMpEbGR8G8Mha2eanNWf-9EtLb5Fe1Ok2dg-pG_8fI4qwYhsmEpwilYLvvxCG_ymlXxhrhen7lZlO_85KS__El7r_DKjeUJXKbPXdGkS_xsEenl75FJ1eXW9MOLYE5McBUo4P-ZCs8m5HPrRRzUgpa_zoVYd9_dXy_nPIiF-kSmVVAT8Q-jBbbDn8xbjM9sQdkJ2EtnswVzRKzs1UH5CGzwtV4lSONkOPBCusmZ9hMiRFIHfQTQl1Amsosr2BlNOAb9CLhO7WklP8Jvtkw")
        .send()
        .onSuccess(response -> {
          if (response.statusCode() == 200){
            promise.complete(new PdaData(response.body()));
          }
          else{
            promise.fail(new PdaException(response.statusCode()));
          }
        })
        .onFailure(err -> {
          System.out.println("FAILED TO REQUEST " + err.getMessage());
          promise.fail(err);
        });

    return promise.future();
  }
}
