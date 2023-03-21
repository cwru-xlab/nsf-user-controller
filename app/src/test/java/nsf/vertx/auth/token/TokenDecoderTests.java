package nsf.vertx.auth.token;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vertx.auth.KeyPairs;
import vertx.auth.Tokens;

public class TokenDecoderTests {

  private static final String TOKEN = Tokens.encodedValid(Tokens.issuedAt(), Tokens.expiresAt());

  @Test
  public void whenTokenIsValidThenTokenDecoderDoesNotThrowException() {
    assertDoesNotThrow(decode(TOKEN));
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
  public void whenTokenIsExpiredThenTokenDecoderThrowsException() {
    Throwable thrown = assertThrowsExactly(TokenException.class, decodeExpired());
    assertEquals(ExpiredJwtException.class, thrown.getCause().getClass());
  }

  private static Stream<Arguments> invalidTokens() {
    return Stream.of(
        Arguments.of(
            "MissingIssuerClaim",
            Tokens.encodedMissingIssuerClaim(Tokens.issuedAt(), Tokens.expiresAt()),
            MissingClaimException.class),
        Arguments.of(
            "MissingIssuedAtClaim",
            Tokens.encodedMissingIssuedAtClaim(Tokens.expiresAt()),
            MissingClaimException.class),
        Arguments.of(
            "MissingExpiresAtClaim",
            Tokens.encodedMissingExpiresAt(Tokens.issuedAt()),
            MissingClaimException.class),
        Arguments.of(
            "MissingResourceClaim",
            Tokens.encodedMissingResourceClaim(Tokens.issuedAt(), Tokens.expiresAt()),
            MissingClaimException.class),
        Arguments.of(
            "MissingAccessScopeClaim",
            Tokens.encodedMissingAccessScopeClaim(Tokens.issuedAt(), Tokens.expiresAt()),
            MissingClaimException.class),
        Arguments.of(
            "IncorrectPrivateKey",
            Tokens.encodedIncorrectPrivateKey(Tokens.issuedAt(), Tokens.expiresAt()),
            SignatureException.class),
        Arguments.of(
            "EmptyToken",
            Tokens.encodedEmpty(),
            IllegalArgumentException.class));
  }

  private static Executable decode(String encoded) {
    return () -> decode(encoded, Tokens.now());
  }

  private static Executable decodeExpired() {
    return () -> decode(TOKEN, Tokens.afterExpiration());
  }

  private static void decode(String encoded, Instant clockTime) {
    TokenDecoder.builder()
        .host(Tokens.host())
        .clock(Clock.fixed(clockTime, ZoneOffset.UTC))
        .build()
        .decode(encoded, KeyPairs.correctPublic());
  }
}
