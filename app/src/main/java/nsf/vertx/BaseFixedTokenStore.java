package nsf.vertx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.vertx.core.Future;
import java.security.PublicKey;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseFixedTokenStore extends AbstractTokenStore {

  protected abstract String encoded();

  public static TokenStore of(String encoded, TokenVerifier verifier) {
    return FixedTokenStore.builder().encoded(encoded).verifier(verifier).build();
  }

  public static TokenStore of(Jws<Claims> decoded, TokenVerifier verifier) {
    return of(encode(decoded), verifier);
  }

  public static TokenStore of(Token token, TokenVerifier verifier) {
    return of(token.encoded(), verifier);
  }

  @SuppressWarnings("unchecked")
  private static String encode(Jws<Claims> decoded) {
    return Jwts.builder()
        .setHeader((Map<String, Object>) decoded.getHeader())
        .setClaims(decoded.getBody())
        .compact();
  }

  @Override
  protected Future<String> getUnverified(PublicKey publicKey) {
    return Future.succeededFuture(encoded());
  }
}
