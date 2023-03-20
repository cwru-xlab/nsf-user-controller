package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.PublicKey;
import org.junit.jupiter.api.Test;
import vertx.KeyPairs;

public class FixedPublicKeyStoreTests {

  @Test
  public void whenPublicKeyIsRetrievedThenFutureResultEqualsConstructorValue() {
    PublicKey publicKey = KeyPairs.correctPublic();
    PublicKeyStore keyStore = FixedPublicKeyStore.of(publicKey);
    assertEquals(publicKey, keyStore.get().result());
  }
}
