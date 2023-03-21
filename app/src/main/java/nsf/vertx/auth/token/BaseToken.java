package nsf.vertx.auth.token;

import static com.google.common.base.Preconditions.checkNotNull;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Duration;
import java.time.Instant;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseToken {

  @Value.Parameter(order = 1)
  public abstract String encoded();

  @Value.Parameter(order = 2)
  public abstract Jws<Claims> decoded();

  public Duration timeToLive() {
    return Duration.between(issuedAt(), expiresAt());
  }

  private Instant issuedAt() {
    return checkNotNull(decoded().getBody().getIssuedAt(), Claims.ISSUED_AT).toInstant();
  }

  private Instant expiresAt() {
    return checkNotNull(decoded().getBody().getExpiration(), Claims.EXPIRATION).toInstant();
  }
}
