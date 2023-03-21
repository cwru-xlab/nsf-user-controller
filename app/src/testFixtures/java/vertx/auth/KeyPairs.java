package vertx.auth;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class KeyPairs {

  private static final KeyPair CORRECT = Keys.keyPairFor(SignatureAlgorithm.RS256);
  private static final KeyPair INCORRECT = Keys.keyPairFor(SignatureAlgorithm.RS256);

  private KeyPairs() {
  }

  public static PublicKey correctPublic() {
    return CORRECT.getPublic();
  }

  public static PublicKey incorrectPublic() {
    return INCORRECT.getPublic();
  }

  public static PrivateKey correctPrivate() {
    return CORRECT.getPrivate();
  }

  public static PrivateKey incorrectPrivate() {
    return INCORRECT.getPrivate();
  }
}
