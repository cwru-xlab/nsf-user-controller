package nsf.vertx;

import io.vertx.core.Future;
import java.security.PublicKey;

public abstract class AbstractTokenStore implements TokenStore {

  protected abstract TokenVerifier verifier();

  protected abstract Future<String> getUnverified(PublicKey publicKey);

  @Override
  public Future<Token> get(PublicKey publicKey) {
    return getUnverified(publicKey).flatMap(token -> verifier().verify(token, publicKey));
  }
}
