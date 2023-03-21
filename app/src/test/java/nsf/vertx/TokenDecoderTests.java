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

public class TokenDecoderTests {

  private static final Duration TIME_TO_LIVE = Duration.ofDays(2);
  private static final Instant ISSUED_AT = Instant.EPOCH;
  private static final Instant EXPIRES_AT = ISSUED_AT.plus(TIME_TO_LIVE);
  private static final Instant NOW = ISSUED_AT.plus(TIME_TO_LIVE.dividedBy(2));
  private static final Instant AFTER_EXPIRATION = EXPIRES_AT.plus(Duration.ofMillis(1));
  private static final String VALID_TOKEN = Tokens.encodedValid(ISSUED_AT, EXPIRES_AT);

  @Test
  public void whenTokenIsValidThenTokenDecoderDoesNotThrowException() {
    assertDoesNotThrow(decode(VALID_TOKEN));
  }

  @ParameterizedTest(name = ParameterizedTest.INDEX_PLACEHOLDER + " {0}: {2}")
  @MethodSource("invalidTokens")
  @SuppressWarnings("unused")
  public void whenTokenIsInvalidThenTokenDecoderThrowsException(
      String testName, String token, Class<? extends Throwable> cause) {
    Throwable thrown = assertThrowsExactly(TokenException.class, decode(token));
    assertEquals(cause, thrown.getCause().getClass());
  }

  @Test
  public void whenTimeIsAfterExpThenTokenDecoderThrowsException() {
    Throwable thrown = assertThrowsExactly(TokenException.class, decodeExpired());
    assertEquals(ExpiredJwtException.class, thrown.getCause().getClass());
  }

  private static Stream<Arguments> invalidTokens() {
    return Stream.of(
        Arguments.of(
            "MissingIssuerClaim",
            Tokens.encodedMissingIssuerClaim(ISSUED_AT, EXPIRES_AT),
            MissingClaimException.class),
        Arguments.of(
            "MissingIssuedAtClaim",
            Tokens.encodedMissingIssuedAtClaim(EXPIRES_AT),
            MissingClaimException.class),
        Arguments.of(
            "MissingExpiresAtClaim",
            Tokens.encodedMissingExpiresAt(ISSUED_AT),
            MissingClaimException.class),
        Arguments.of(
            "MissingResourceClaim",
            Tokens.encodedMissingResourceClaim(ISSUED_AT, EXPIRES_AT),
            MissingClaimException.class),
        Arguments.of(
            "MissingAccessScopeClaim",
            Tokens.encodedMissingAccessScopeClaim(ISSUED_AT, EXPIRES_AT),
            MissingClaimException.class),
        Arguments.of(
            "IncorrectPrivateKey",
            Tokens.encodedIncorrectPrivateKey(ISSUED_AT, EXPIRES_AT),
            SignatureException.class),
        Arguments.of(
            "EmptyToken",
            Tokens.encodedEmpty(),
            IllegalArgumentException.class));
  }

  private static Executable decode(String encoded) {
    return () -> decode(encoded, NOW);
  }

  private static Executable decodeExpired() {
    return () -> decode(VALID_TOKEN, AFTER_EXPIRATION);
  }

  private static void decode(String encoded, Instant now) {
    TokenDecoder.builder()
        .host(Tokens.host())
        .clock(Clock.fixed(now, ZoneOffset.UTC))
        .build()
        .decode(encoded, KeyPairs.correctPublic());
  }
}
