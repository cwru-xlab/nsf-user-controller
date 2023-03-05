package nsf.pda.auth;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.immutables.value.Value;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Value.Immutable
abstract class BaseTokenService implements AutoCloseable {

  public static final String TOKEN_HEADER = "x-auth-token";

  private static final long INITIAL_REFRESH_DELAY = 0;
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

  @Value.Default
  protected Duration timeout() {
    return Duration.ofSeconds(5);
  }

  @Value.Derived
  protected AtomicReference<AccessToken> token() {
    return new AtomicReference<>();
  }

  @Value.Derived
  protected Callback<AccessToken> callback() {
    return new TokenCallback(this);
  }

  @Value.Derived
  protected ScheduledFuture<?> scheduledFetchToken() {
    return scheduler().scheduleWithFixedDelay(
        this::fetchAndSetToken, INITIAL_REFRESH_DELAY, tokenTtl().getSeconds(), TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    scheduler().shutdown();
  }

  // TODO How to avoid race condition between initial setting and getting?
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
    String username = credentials().username();
    String password = credentials().password();
    Call<AccessToken> call = authenticator().authenticate(username, password);
    call.enqueue(callback());
  }

  private static final class TokenCallback implements Callback<AccessToken> {

    private final BaseTokenService tokenService;

    public TokenCallback(BaseTokenService tokenService) {
      this.tokenService = tokenService;
    }

    @Override
    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
      tokenService.setToken(response.body());
      System.out.println("Setting new token: " + tokenService.getToken());
    }

    @Override
    public void onFailure(Call<AccessToken> call, Throwable throwable) {
      System.out.println("Unable to fetch token: " + throwable);
    }
  }
}
