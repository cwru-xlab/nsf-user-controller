package nsf.vertx.auth.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import io.vertx.core.Future;
import java.security.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import vertx.auth.KeyPairs;
import vertx.auth.Tokens;

@ExtendWith(MockitoExtension.class)
public class FixedTokenStoreTests {

  private static final Token TOKEN = Tokens.valid(Tokens.issuedAt(), Tokens.expiresAt());
  private static final Class<? extends Throwable> EXCEPTION = TokenException.class;

  @Mock(stubOnly = true)
  private TokenDecoder decoder;
  private TokenStore tokenStore;

  @BeforeEach
  public void setUp() {
    tokenStore = FixedTokenStore.of(TOKEN.encoded(), decoder);
  }

  @Test
  public void whenDecodingSucceedsThenTokenStoreReturnsConstructorValue() {
    whenDecoded().thenReturn(TOKEN);
    assertEquals(TOKEN, getToken().result());
  }

  @Test
  public void whenDecodingFailsThenTokenStoreThrowsException() {
    whenDecoded().thenThrow(EXCEPTION);
    assertEquals(EXCEPTION, getToken().cause().getClass());
  }

  private OngoingStubbing<Token> whenDecoded() {
    return when(decoder.decode(anyString(), any(PublicKey.class)));
  }

  private Future<Token> getToken() {
    return tokenStore.get(KeyPairs.correctPublic());
  }
}
