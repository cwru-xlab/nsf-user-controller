package nsf.pda;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsf.pda.auth.AuthInterceptor;
import nsf.pda.auth.Authenticator;
import nsf.pda.auth.BasicCredentials;
import nsf.pda.auth.TokenService;
import nsf.pda.data.DataService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class Example {

  public static void main(String[] args) {
    ObjectMapper mapper = new ObjectMapper();
    OkHttpClient client = new OkHttpClient.Builder()
        .build();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://username.hubat.net/")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .client(client)
        .build();
    Authenticator authenticator = retrofit.create(Authenticator.class);
    TokenService tokenService = TokenService.builder()
        .credentials(BasicCredentials.of("username", "password"))
        .authenticator(authenticator)
        .build();
    OkHttpClient authenticatedClient = client.newBuilder()
        .addInterceptor(AuthInterceptor.of(tokenService))
        .build();
    Retrofit authenticatedRetrofit = retrofit.newBuilder()
        .baseUrl("https://username.hubat.net/api/v2.6/")
        .client(authenticatedClient)
        .build();
    DataService dataService = authenticatedRetrofit.create(DataService.class);
  }
}
