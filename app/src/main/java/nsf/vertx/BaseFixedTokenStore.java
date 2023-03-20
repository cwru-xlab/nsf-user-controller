package nsf.vertx;

import io.vertx.core.Future;
import java.security.PublicKey;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseFixedTokenStore extends AbstractTokenStore {

  protected abstract String encoded();

  public static TokenStore of(String encoded, TokenVerifier verifier) {
    return FixedTokenStore.builder().encoded(encoded).verifier(verifier).build();
  }

  @Override
  protected Future<String> getUnverified(PublicKey publicKey) {
    return Future.succeededFuture(encoded());
  }
}
