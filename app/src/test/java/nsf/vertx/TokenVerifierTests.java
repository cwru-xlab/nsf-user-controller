package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.Future;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vertx.KeyPairs;
import vertx.Tokens;

public class TokenVerifierTests {

  private static final Duration TTL = Duration.ofDays(2);
  private static final Instant IAT = Instant.EPOCH;
  private static final Instant EXP = IAT.plus(TTL);
  private static final Instant NOW = IAT.plus(TTL.dividedBy(2));
  private static final Instant AFTER_EXP = EXP.plus(Duration.ofMillis(1));

  @Test
  public void whenTokenIsValidThenTokenVerifierReturnsSucceededFuture() {
    String token = Tokens.valid(IAT, EXP);
    assertSucceeded(verifyValid(token));
  }

  @ParameterizedTest
  @MethodSource("invalidTokens")
  public void whenTokenIsInvalidThenTokenVerifierReturnsFailedFuture(String token) {
    assertFailed(verifyValid(token));
  }

  @Test
  public void whenTimeIsAfterExpThenTokenVerifierReturnsFailedFuture() {
    String token = Tokens.valid(IAT, EXP);
    assertFailed(verifyExpired(token));
  }

  private static Stream<Arguments> invalidTokens() {
    return Stream.of(
            Tokens.missingIssClaim(IAT, EXP),
            Tokens.missingIatClaim(EXP),
            Tokens.missingExpClaim(IAT),
            Tokens.missingResourceClaim(IAT, EXP),
            Tokens.missingAccessScopeClaim(IAT, EXP),
            Tokens.wrongPrivateKey(IAT, EXP),
            Tokens.empty())
        .map(Arguments::of);
  }

  private static void assertSucceeded(Future<Token> future) {
    assertTrue(future.succeeded());
  }

  private static void assertFailed(Future<Token> future) {
    assertTrue(future.failed());
  }

  private static Future<Token> verifyValid(String encoded) {
    return verify(encoded, NOW);
  }

  private static Future<Token> verifyExpired(String encoded) {
    return verify(encoded, AFTER_EXP);
  }

  private static Future<Token> verify(String encoded, Instant now) {
    return TokenVerifier.builder()
        .host(Tokens.host())
        .clock(Clock.fixed(now, ZoneOffset.UTC))
        .build()
        .verify(encoded, KeyPairs.correctPublic());
  }
}
