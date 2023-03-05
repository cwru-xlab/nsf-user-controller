package nsf.pda;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class Example {

  public static void main(String[] args) {
    ObjectMapper mapper = new ObjectMapper();
    OkHttpClient client = new OkHttpClient.Builder().build();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://example.hubat.net/api/v2.6/")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .client(client)
        .build();
    DataService dataService = retrofit.create(DataService.class);
  }
}
