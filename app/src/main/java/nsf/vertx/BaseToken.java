package nsf.vertx;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseToken {

  private static final String IAT_PATH = "decoded.body.iat";
  private static final String EXP_PATH = "decoded.body.exp";

  @Value.Parameter(order = 1)
  public abstract String encoded();

  @Value.Parameter(order = 2)
  public abstract Jwt<?, Claims> decoded();

  public Duration ttl() {
    Instant iat = iat().toInstant();
    Instant exp = exp().toInstant();
    return Duration.between(iat, exp);
  }

  private Date iat() {
    return decoded().getBody().getIssuedAt();
  }

  private Date exp() {
    return decoded().getBody().getExpiration();
  }

  @Value.Check
  protected void check() {
    checkClaimIsPresent(iat(), IAT_PATH);
    checkClaimIsPresent(exp(), IAT_PATH);
    checkIatIsBeforeExp();
  }

  private static void checkClaimIsPresent(Object claim, String path) {
    checkNotNull(claim, "'" + path + "' is required");
  }

  private void checkIatIsBeforeExp() {
    checkState(iat().before(exp()), "'" + IAT_PATH + "' must be before '" + EXP_PATH + "'");
  }
}
