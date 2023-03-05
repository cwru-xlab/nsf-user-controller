package nsf.pda;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsf.pda.auth.Authenticator;
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
        .baseUrl("https://example.hubat.net/")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .client(client)
        .build();
    Authenticator authService = retrofit.create(Authenticator.class);
    OkHttpClient authenticatedClient = client.newBuilder()
        .build();
    Retrofit authenticatedRetrofit = retrofit.newBuilder()
        .baseUrl("https://example.hubat.net/api/v2.6/")
        .client(authenticatedClient)
        .build();
    DataService dataService = authenticatedRetrofit.create(DataService.class);
  }
}
