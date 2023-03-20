package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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
  private static final String VALID_TOKEN = Tokens.encodedValid(IAT, EXP);

  @Test
  public void whenTokenIsValidThenTokenVerifierDoesNotThrowException() {
    assertDoesNotThrow(verify(VALID_TOKEN));
  }

  @ParameterizedTest(name = ParameterizedTest.INDEX_PLACEHOLDER + " {0}: {2}")
  @MethodSource("invalidTokens")
  @SuppressWarnings("unused")
  public void whenTokenIsInvalidThenTokenVerifierThrowsException(
      String testName, String token, Class<? extends Throwable> cause) {
    Throwable thrown = assertThrowsExactly(TokenException.class, verify(token));
    assertEquals(cause, thrown.getCause().getClass());
  }

  @Test
  public void whenTimeIsAfterExpThenTokenVerifierThrowsException() {
    Throwable thrown = assertThrowsExactly(TokenException.class, verifyExpired());
    assertEquals(ExpiredJwtException.class, thrown.getCause().getClass());
  }

  private static Stream<Arguments> invalidTokens() {
    return Stream.of(
        Arguments.of(
            "MissingIssClaim",
            Tokens.encodedMissingIssClaim(IAT, EXP),
            MissingClaimException.class),
        Arguments.of(
            "MissingIatClaim",
            Tokens.encodedMissingIatClaim(EXP),
            MissingClaimException.class),
        Arguments.of(
            "MissingExpClaim",
            Tokens.encodedMissingExpClaim(IAT),
            MissingClaimException.class),
        Arguments.of(
            "MissingResourceClaim",
            Tokens.encodedMissingResourceClaim(IAT, EXP),
            MissingClaimException.class),
        Arguments.of(
            "MissingAccessScopeClaim",
            Tokens.encodedMissingAccessScopeClaim(IAT, EXP),
            MissingClaimException.class),
        Arguments.of(
            "WrongPrivateKey",
            Tokens.encodedWrongPrivateKey(IAT, EXP),
            SignatureException.class),
        Arguments.of(
            "EmptyToken",
            Tokens.encodedEmpty(),
            IllegalArgumentException.class));
  }

  private static Executable verify(String encoded) {
    return () -> verify(encoded, NOW);
  }

  private static Executable verifyExpired() {
    return () -> verify(VALID_TOKEN, AFTER_EXP);
  }

  private static void verify(String encoded, Instant now) {
    TokenVerifier.builder()
        .host(Tokens.host())
        .clock(Clock.fixed(now, ZoneOffset.UTC))
        .build()
        .verify(encoded, KeyPairs.correctPublic());
  }
}
