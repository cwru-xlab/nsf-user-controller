package nsf.vertx.auth.token;

import io.vertx.junit5.VertxExtension;
import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TokenServiceTests {

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