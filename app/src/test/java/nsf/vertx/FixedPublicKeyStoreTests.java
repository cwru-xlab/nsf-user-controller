package nsf.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.PublicKey;
import org.junit.jupiter.api.Test;
import vertx.KeyPairs;

public class FixedPublicKeyStoreTests {

  @Test
  public void futureResultEqualsConstructorValue() {
    PublicKey publicKey = KeyPairs.correctPublic();
    PublicKeyStore publicKeyStore = FixedPublicKeyStore.of(publicKey);
    assertEquals(publicKey, publicKeyStore.get().result());
  }
}
