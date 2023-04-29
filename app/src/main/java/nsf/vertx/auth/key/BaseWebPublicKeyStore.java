package nsf.vertx.auth.key;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import java.security.PublicKey;
import nsf.vertx.auth.WebStore;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseWebPublicKeyStore implements PublicKeyStore, WebStore {

  private static final Logger logger = LoggerFactory.getLogger(WebPublicKeyStore.class);

  public static PublicKeyStore of(WebClient client, String host) {
    return WebPublicKeyStore.builder().client(client).host(host).build();
  }

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

  private static final class Rsa256PublicKey implements PublicKey {

    private final byte[] encoded;

    private Rsa256PublicKey(byte[] encoded) {
      this.encoded = encoded;
    }

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
