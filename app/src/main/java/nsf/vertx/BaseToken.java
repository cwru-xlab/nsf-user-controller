package nsf.vertx;

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

  public Duration ttl() {
    return Duration.between(iat(), exp());
  }

  private Instant iat() {
    return checkNotNull(decoded().getBody().getIssuedAt(), "iat").toInstant();
  }

  private Instant exp() {
    return checkNotNull(decoded().getBody().getExpiration(), "exp").toInstant();
  }
}
