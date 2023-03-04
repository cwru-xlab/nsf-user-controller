package nsf.pda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// TODO See https://github.com/cwru-xlab/hat-py-sdk/blob/main/src/hat/errors.py for error codes

public class PdaClient {

  private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

  // TODO Configure to include the base URL: {scheme}://{username}.{domain}/api/v2.6
  private final OkHttpClient httpClient = null;
  private final ObjectMapper mapper = null;

   /*
  GET: {scheme}://{username}.{domain}/api/v2.6/data/namespace/endpoint

  POST: {scheme}://{username}.{domain}/api/v2.6/data/namespace/endpoint

  PUT: {scheme}://{username}.{domain}/api/v2.6/data

  DELETE: {scheme}://{username}.{domain}/api/v2.6/data?records={recordId}&records=...
   */

  public Object get(String endpoint) throws IOException {
    HttpUrl url = newUrlBuilder()
        .addPathSegment(endpoint)
        .build();
    Request request = newRequestBuilder()
        .method("GET", RequestBody.create("", MEDIA_TYPE))
        .url(url)
        .addHeader("GET", null)
        .addHeader("x-auth-token", null) // TODO Consider using Interceptor
        .build();
    try (Response response = execute(request)) {
      if (response.isSuccessful()) {
        JsonNode root = mapper.readTree(response.body().byteStream());
        List<Record> records = Streams.stream(root.elements())
            .map(Record::from)
            .collect(Collectors.toList());
        // Extract data from records and return resulting array
      } else {
        // Handle error
      }
    }
    return null;
  }

  public Object put(Object request) {
    return null;
  }

  public Object post(Object request) {
    return null;
  }

  public Object delete(Object request) {
    return null;
  }

//  private Object handleResponse(Response response) {
//    if (response.isSuccessful()) {
//      return null;
//    }
//
//    switch (response.code()) {
//      case 400:
//        return null;
//    }
//  }

  private Request.Builder newRequestBuilder() {
    return new Request.Builder().addHeader("Content-Type", "application/json");
  }

  private HttpUrl.Builder newUrlBuilder() {
    return new HttpUrl.Builder().addPathSegment("data");
  }

  private Response execute(Request request) throws IOException {
    return httpClient.newCall(request).execute();
  }
}
