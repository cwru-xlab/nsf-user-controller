package nsf.vertx.auth.token;

import io.vertx.core.Future;
import java.security.PublicKey;
import nsf.vertx.auth.FixedValueStore;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseFixedTokenStore extends AbstractTokenStore implements FixedValueStore<String> {

  public static TokenStore of(String value, TokenDecoder decoder) {
    return FixedTokenStore.builder().value(value).decoder(decoder).build();
  }

  @Override
  protected Future<String> getEncoded(PublicKey publicKey) {
    return Future.succeededFuture(value());
  }
}
