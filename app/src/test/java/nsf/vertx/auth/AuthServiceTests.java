package nsf.vertx.auth;

import io.vertx.junit5.VertxExtension;
import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

@Disabled("Disabled until MockWebServer issues are resolved")
@ExtendWith(VertxExtension.class)
public class AuthServiceTests {

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