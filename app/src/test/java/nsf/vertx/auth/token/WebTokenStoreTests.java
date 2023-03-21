package nsf.vertx.auth.token;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

@Disabled("Disabled until MockWebServer issues are resolved")
public class WebTokenStoreTests {

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
