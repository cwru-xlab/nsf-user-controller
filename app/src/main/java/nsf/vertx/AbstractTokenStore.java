package nsf.vertx;

import io.vertx.core.Future;
import java.security.PublicKey;

public abstract class AbstractTokenStore implements TokenStore {

  protected abstract TokenDecoder decoder();

  protected abstract Future<String> getEncoded(PublicKey publicKey);

  @Override
  public Future<Token> get(PublicKey publicKey) {
    return getEncoded(publicKey).map(token -> decoder().decode(token, publicKey));
  }
}
