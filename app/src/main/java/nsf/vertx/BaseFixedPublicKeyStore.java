package nsf.vertx;

import io.vertx.core.Future;
import java.security.PublicKey;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseFixedPublicKeyStore implements PublicKeyStore {

  @Value.Parameter
  protected abstract PublicKey value();

  @Override
  public Future<PublicKey> get() {
    return Future.succeededFuture(value());
  }
}
