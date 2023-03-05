package nsf.pda.auth;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.immutables.value.Value;

@Value.Immutable
abstract class BaseAuthInterceptor implements Interceptor {

  @Value.Parameter
  protected abstract TokenService tokenService();

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = addTokenHeader(chain.request());
    Response response = chain.proceed(request);
    tokenService().renewToken(response);
    return response;
  }

  private Request addTokenHeader(Request request) {
    AccessToken token = tokenService().getToken();
    return request.newBuilder()
        .header(TokenService.TOKEN_HEADER, token.value())
        .build();
  }
}
