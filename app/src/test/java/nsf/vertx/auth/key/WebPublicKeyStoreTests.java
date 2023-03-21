package nsf.vertx.auth.key;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import vertx.auth.KeyPairs;

@Disabled("Disabled until MockWebServer issues are resolved")
@ExtendWith(VertxExtension.class)
public class WebPublicKeyStoreTests {

  private static MockWebServer server;

  private WebClient client;
  private PublicKeyStore keyStore;

  @BeforeAll
  public static void beforeAll() throws IOException {
    server = new MockWebServer();
    server.start(8080);
  }

  @AfterAll
  public static void afterAll() throws IOException {
    server.shutdown();
  }

  @BeforeEach
  public void beforeEach(Vertx vertx) {
    client = WebClient.create(vertx);
    keyStore = WebPublicKeyStore.of(client, server.getHostName());
  }

  @AfterEach
  public void afterEach() {
    client.close();
  }

  @Test
  public void test(VertxTestContext context) {
    String publicKey = new String(KeyPairs.correctPublic().getEncoded(), StandardCharsets.UTF_8);
    MockResponse response = new MockResponse().setBody(publicKey);
    server.enqueue(response);
    keyStore.get().onComplete(
        context.succeeding(pk -> context.verify(() -> {
          assertEquals(publicKey, pk.getEncoded());
          context.completeNow();
        })));
  }
}
