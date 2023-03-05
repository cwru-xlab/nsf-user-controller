package nsf.pda.data;

import java.util.Collection;
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
  Call<List<Record>> get(@Path("endpoint") String endpoint, @Body GetOptions options);

  @GET("data/{endpoint}")
  Call<List<Record>> get(@Path("endpoint") String endpoint);

  @POST("data/{endpoint}")
  Call<List<Record>> post(@Path("endpoint") String endpoint, @Body Collection<Record> records);

  @POST("data/{endpoint}")
  Call<Record> post(@Path("endpoint") String endpoint, @Body Record record);

  @PUT("data")
  Call<List<Record>> put(@Body Collection<Record> records);

  @DELETE("data")
  Call<Void> delete(@Query("records") String... recordIds);

}
