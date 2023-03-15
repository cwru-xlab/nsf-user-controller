package nsf.vertx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.vertx.core.Future;
import java.security.PublicKey;
import java.time.Clock;
import java.util.Date;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseTokenVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TokenVerifier.class);

  protected abstract String host();

  protected abstract Clock clock();

  public Future<Token> verify(String encoded, PublicKey publicKey) {
    try {
      Jws<Claims> decoded = Jwts.parserBuilder()
          .requireIssuer(host())
          .require("resource", host())
          .require("accessScope", "owner")
          .setClock(this::now)
          .setSigningKey(publicKey)
          .build()
          .parseClaimsJws(encoded);
      return Future.succeededFuture(Token.of(encoded, decoded));
    } catch (UnsupportedJwtException | IllegalArgumentException | MalformedJwtException |
             SignatureException | ExpiredJwtException exception) {
      return Future.<Token>failedFuture(exception).onFailure(this::logVerificationFailure);
    }
  }

  private Date now() {
    return Date.from(clock().instant());
  }

  private void logVerificationFailure(Throwable throwable) {
    logger.atError()
        .setCause(throwable)
        .setMessage("Failed to parse or verify access token for host {}")
        .addArgument(host())
        .log();
  }
}
