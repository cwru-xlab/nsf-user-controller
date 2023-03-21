package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vertx.Tokens;

public class TokenTests {

  @Test
  public void whenValidTokenIsCreatedThenTimeToLiveEqualsExpectedValue() {
    Token token = Tokens.valid(Tokens.issuedAt(), Tokens.expiresAt());
    assertEquals(Tokens.timeToLive(), token.timeToLive());
  }

  @ParameterizedTest(name = ParameterizedTest.INDEX_PLACEHOLDER + " {0}: {2}")
  @MethodSource("invalidTokens")
  @SuppressWarnings("unused")
  public void whenInvalidTokenIsCreatedThenTimeToLiveThrowsException(String testName, Token token) {
    assertThrowsExactly(NullPointerException.class, token::timeToLive);
  }

  private static Stream<Arguments> invalidTokens() {
    return Stream.of(
        Arguments.of("MissingIssuedAtClaim", Tokens.missingIssuedAtClaim(Tokens.expiresAt())),
        Arguments.of("MissingExpiresAtClaim", Tokens.missingExpiresAtClaim(Tokens.issuedAt())));
  }
}
