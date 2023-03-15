package nsf.vertx;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import java.security.PublicKey;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseWebPublicKeyStore implements PublicKeyStore {

  private static final Logger logger = LoggerFactory.getLogger(WebPublicKeyStore.class);

  @Value.Parameter(order = 1)
  protected abstract WebClient client();

  @Value.Parameter(order = 2)
  protected abstract String host();

  @Override
  public Future<PublicKey> get() {
    return client()
        .get(host(), "/publickey")
        .as(BodyCodec.create(Rsa256PublicKey::from))
        .send()
        .map(HttpResponse::body)
        .onFailure(this::logFetchFailure);
  }

  private void logFetchFailure(Throwable throwable) {
    logger.atError()
        .setCause(throwable)
        .setMessage("Failed to fetch public key for host {}")
        .addArgument(host())
        .log();
  }

  private record Rsa256PublicKey(byte[] encoded) implements PublicKey {

    public static PublicKey from(Buffer buffer) {
      return new Rsa256PublicKey(buffer.getBytes());
    }

    @Override
    public String getAlgorithm() {
      return "RSA";
    }

    @Override
    public String getFormat() {
      return "X.509";
    }

    @Override
    public byte[] getEncoded() {
      return encoded;
    }
  }
}
