package nsf.pda.auth;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface Authenticator {

  @GET("users/access_token")
  Call<AccessToken> authenticate(
      @Header("username") String username, @Header("password") String password);
}