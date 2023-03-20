package nsf.vertx;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.MissingClaimException;
import java.security.PublicKey;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseTokenVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TokenVerifier.class);

  private static void requireClaim(Jws<Claims> token, Function<Claims, ?> getter, String claim) {
    Optional.of(token)
        .map(Jwt::getBody)
        .map(getter)
        .orElseThrow(() -> missingClaimException(token, claim));
  }

  private static MissingClaimException missingClaimException(Jws<Claims> token, String claim) {
    String template = ClaimJwtException.MISSING_EXPECTED_CLAIM_MESSAGE_TEMPLATE;
    String message = String.format(template, claim, "non-null");
    return new MissingClaimException(token.getHeader(), token.getBody(), message);
  }

  protected abstract String host();

  protected abstract Clock clock();

  public Token verify(String encoded, PublicKey publicKey) {
    JwtParser parser = newParser(publicKey);
    Jws<Claims> decoded;
    try {
      decoded = parser.parseClaimsJws(encoded);
      requireClaim(decoded, Claims::getIssuedAt, Claims.ISSUED_AT);
      requireClaim(decoded, Claims::getExpiration, Claims.EXPIRATION);
    } catch (JwtException | IllegalArgumentException cause) {
      TokenException exception = new TokenException(cause);
      logVerificationFailure(exception);
      throw exception;
    }
    return Token.of(encoded, decoded);
  }

  private JwtParser newParser(PublicKey publicKey) {
    return TokenParserBuilder.create()
        .signingKey(publicKey)
        .clock(clock())
        .host(host())
        .build();
  }

  private void logVerificationFailure(Throwable throwable) {
    logger.atError()
        .setCause(throwable)
        .setMessage("Failed to parse or verify access token for host {}")
        .addArgument(host())
        .log();
  }
}
