package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class TokenTests {

  private static final Duration TTL = Duration.ofDays(1);
  private static final Instant IAT = Instant.EPOCH;
  private static final Date IAT_DATE = Date.from(IAT);
  private static final Instant EXP = IAT.plus(TTL);
  private static final Date EXP_DATE = Date.from(EXP);

  @Test
  public void whenIatIsAfterExpThenIllegalStateException() {
    assertEquals(TTL, token().ttl());
  }

  private static Token token() {
    String encoded = encoded();
    Jwt<?, Claims> decoded = decoded(encoded);
    return Token.of(encoded, decoded);
  }

  private static String encoded() {
    return Jwts.builder()
        .setIssuedAt(IAT_DATE)
        .setExpiration(EXP_DATE)
        .compact();
  }

  private static Jwt<?, Claims> decoded(String encoded) {
    return Jwts.parserBuilder()
        .setClock(() -> IAT_DATE)
        .build()
        .parseClaimsJwt(encoded);
  }
}
