package nsf.pda.auth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.immutables.value.Value;
import retrofit2.Call;

@Value.Immutable
abstract class BaseTokenService implements AutoCloseable {

  public static final String TOKEN_HEADER = "x-auth-token";

  private static final String UNABLE_TO_RENEW_TOKEN_MSG =
      "Unable to renew token; the '" + TOKEN_HEADER + "' header is missing";

  protected abstract Credentials credentials();

  protected abstract Authenticator authenticator();

  @Value.Default
  protected ScheduledExecutorService scheduler() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  @Value.Default
  protected Duration tokenTtl() {
    return Duration.ofDays(29);
  }

  @Value.Derived
  protected AtomicReference<AccessToken> token() {
    return new AtomicReference<>();
  }

  @Value.Derived
  protected ScheduledFuture<?> scheduledFetchToken() {
    // Make a blocking call to initialize the token value to avoid race conditions...
    fetchAndSetToken();
    // ...and then rely on the scheduler to update it in the background.
    long delay = tokenTtl().getSeconds();
    return scheduler().scheduleWithFixedDelay(
        this::fetchAndSetToken, delay, delay, TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    scheduler().shutdown();
  }

  public AccessToken getToken() {
    return token().get();
  }

  public void setToken(AccessToken token) {
    token().set(checkNotNull(token));
  }

  public void renewToken(okhttp3.Response response) {
    String value = checkNotNull(response.headers().get(TOKEN_HEADER), UNABLE_TO_RENEW_TOKEN_MSG);
    token().getAndUpdate(t -> t.withValue(value));
  }

  private void fetchAndSetToken() {
    setToken(fetchToken());
  }

  private Call<AccessToken> fetchToken() {
    String username = credentials().username();
    String password = credentials().password();
    return authenticator().authenticate(username, password);
  }

  private void setToken(Call<AccessToken> call) {
    try {
      setToken(call.execute().body());
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
