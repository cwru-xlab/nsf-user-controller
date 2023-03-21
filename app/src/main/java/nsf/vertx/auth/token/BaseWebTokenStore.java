package nsf.vertx.auth.token;

import io.vertx.core.Future;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import java.security.PublicKey;
import nsf.vertx.auth.WebStore;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseWebTokenStore extends AbstractTokenStore implements WebStore {

  private static final Logger logger = LoggerFactory.getLogger(WebTokenStore.class);

  public static TokenStore of(WebClient client, String host, TokenDecoder decoder) {
    return WebTokenStore.builder().host(host).client(client).decoder(decoder).build();
  }

  @Override
  public Future<String> getEncoded(PublicKey publicKey) {
    return client()
        .get(host(), "/access_token")
        .as(BodyCodec.jsonObject())
        .send()
        .map(HttpResponse::body)
        .map(body -> body.getString("accessToken"))
        .onFailure(this::logFetchFailure);
  }

  private void logFetchFailure(Throwable throwable) {
    logger.atError()
        .setCause(throwable)
        .setMessage("Failed to fetch access token for host {}")
        .addArgument(host())
        .log();
  }
}
