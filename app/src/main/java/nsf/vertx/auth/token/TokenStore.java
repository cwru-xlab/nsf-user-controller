package nsf.vertx.auth.token;

import io.vertx.core.Future;
import java.security.PublicKey;

public interface TokenStore {

  Future<Token> get(PublicKey publicKey);
}
