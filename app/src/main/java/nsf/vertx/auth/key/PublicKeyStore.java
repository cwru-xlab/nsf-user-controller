package nsf.vertx.auth.key;

import io.vertx.core.Future;
import java.security.PublicKey;

public interface PublicKeyStore {

  Future<PublicKey> get();
}
