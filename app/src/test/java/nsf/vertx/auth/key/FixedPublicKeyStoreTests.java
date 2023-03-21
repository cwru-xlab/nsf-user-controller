package nsf.vertx.auth.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.PublicKey;
import org.junit.jupiter.api.Test;
import vertx.auth.KeyPairs;

public class FixedPublicKeyStoreTests {

  @Test
  public void whenPublicKeyIsRetrievedThenFutureEqualsConstructorValue() {
    PublicKey publicKey = KeyPairs.correctPublic();
    PublicKeyStore keyStore = FixedPublicKeyStore.of(publicKey);
    assertEquals(publicKey, keyStore.get().result());
  }
}
