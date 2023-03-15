package nsf.vertx;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class WebPublicKeyStoreTests {

  private static MockWebServer server;

  @BeforeAll
  public static void beforeAll() {
    server = new MockWebServer();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    server.shutdown();
  }
}
