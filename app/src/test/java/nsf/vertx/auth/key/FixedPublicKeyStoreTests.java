package nsf.vertx.auth.key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.PublicKey;
import org.junit.jupiter.api.Test;
import vertx.auth.KeyPairs;

public class FixedPublicKeyStoreTests {

  private static final PublicKey CORRECT = KeyPairs.correctPublic();
  private static final PublicKey INCORRECT = KeyPairs.incorrectPublic();

  @Test
  public void whenPublicKeyIsRetrievedThenFutureEqualsConstructorValue() {
    PublicKeyStore keyStore = FixedPublicKeyStore.of(CORRECT);
    PublicKey keyStoreValue = keyStore.get().result();
    assertEquals(CORRECT, keyStoreValue);
    assertNotEquals(INCORRECT, keyStoreValue);
  }
}
