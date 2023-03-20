package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.security.SignatureException;
import io.vertx.core.Future;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import vertx.KeyPairs;
import vertx.Tokens;

@ExtendWith(MockitoExtension.class)
public class FixedTokenStoreTests {

  private static final Instant IAT = Instant.EPOCH;
  private static final Instant EXP = IAT.plus(Duration.ofDays(1));
  private static final Token TOKEN = Tokens.valid(IAT, EXP);

  private static final Class<? extends Throwable> EXCEPTION = SignatureException.class;

  @Mock(stubOnly = true)
  private TokenVerifier verifier;
  private TokenStore tokenStore;

  @BeforeEach
  public void setUp() {
    tokenStore = FixedTokenStore.of(TOKEN.encoded(), verifier);
  }

  @Test
  public void whenTokenIsVerifiedThenTokenStoreReturnsConstructorValue() {
    whenVerifierVerifies().thenReturn(TOKEN);
    assertEquals(TOKEN, getToken().result());
  }

  @Test
  public void whenTokenIsUnverifiedThenTokenStoreThrowsException() {
    whenVerifierVerifies().thenThrow(EXCEPTION);
    assertEquals(EXCEPTION, getToken().cause().getClass());
  }

  private OngoingStubbing<Token> whenVerifierVerifies() {
    return when(verifier.verify(anyString(), any(PublicKey.class)));
  }

  private Future<Token> getToken() {
    return tokenStore.get(KeyPairs.correctPublic());
  }
}
