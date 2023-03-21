package nsf.vertx.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import java.security.PublicKey;
import java.time.Clock;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
abstract class BaseTokenDecoder {

  private static final Logger logger = LoggerFactory.getLogger(TokenDecoder.class);

  protected abstract String host();

  protected abstract Clock clock();

  public Token decode(String encoded, PublicKey publicKey) {
    Jws<Claims> decoded;
    try {
      decoded = DecodedTokenBuilder.create()
          .encoded(encoded)
          .publicKey(publicKey)
          .host(host())
          .clock(clock())
          .strict(true)
          .build();
    } catch (JwtException | IllegalArgumentException cause) {
      TokenException exception = new TokenException(cause);
      logDecodingFailure(exception);
      throw exception;
    }
    return Token.of(encoded, decoded);
  }

  private void logDecodingFailure(Throwable throwable) {
    logger.atError()
        .setCause(throwable)
        .setMessage("Failed to decode access token for host {}")
        .addArgument(host())
        .log();
  }
}
