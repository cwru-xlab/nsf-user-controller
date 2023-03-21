package nsf.vertx;

import io.vertx.core.Future;
import java.security.PublicKey;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseFixedPublicKeyStore implements PublicKeyStore, FixedValueStore<PublicKey> {

  public static PublicKeyStore of(PublicKey value) {
    return FixedPublicKeyStore.builder().value(value).build();
  }

  @Override
  public Future<PublicKey> get() {
    return Future.succeededFuture(value());
  }
}
