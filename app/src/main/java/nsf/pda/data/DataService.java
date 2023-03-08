package nsf.pda.data;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DataService {

  @GET("data/{endpoint}")
  Call<List<Record>> get(
      @Path(value = "endpoint", encoded = true) String endpoint, @Body GetOptions options);

  @GET("data/{endpoint}")
  Call<List<Record>> get(@Path(value = "endpoint", encoded = true) String endpoint);

  @POST("data/{endpoint}")
  Call<List<Record>> post(
      @Path(value = "endpoint", encoded = true) String endpoint, @Body List<Record> data);

  @POST("data/{endpoint}")
  Call<Record> post(
      @Path(value = "endpoint", encoded = true) String endpoint, @Body Record data);

  @PUT("data")
  Call<List<Record>> put(@Body List<Record> records);

  @DELETE("data")
  Call<Void> delete(@Query("records") String... recordIds);

  @DELETE("data")
  Call<Void> delete(@Query("records") List<String> recordIds);
}
