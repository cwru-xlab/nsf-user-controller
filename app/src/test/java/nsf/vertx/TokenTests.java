package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import vertx.Tokens;

public class TokenTests {

  private static final Duration TTL = Duration.ofDays(1);
  private static final Instant IAT = Instant.EPOCH;
  private static final Instant EXP = IAT.plus(TTL);

  @Test
  public void whenIatIsAfterExpThenIllegalStateException() {
    assertEquals(TTL, Tokens.valid(IAT, EXP).ttl());
  }
}
