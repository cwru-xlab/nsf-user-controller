package nsf.pda;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface AuthService {

  @GET("users/access_token")
  Call<AccessToken> auth(@Header("username") String username, @Header("password") String password);

}
