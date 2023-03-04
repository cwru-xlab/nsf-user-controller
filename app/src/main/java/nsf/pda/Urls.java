package nsf.pda;

import java.net.URL;
import okhttp3.HttpUrl;
import org.immutables.builder.Builder;

class Urls {

  /*
  GET: {scheme}://{username}.{domain}/api/v2.6/data/namespace/endpoint

  POST: {scheme}://{username}.{domain}/api/v2.6/data/namespace/endpoint

  PUT: {scheme}://{username}.{domain}/api/v2.6/data

  DELETE: {scheme}://{username}.{domain}/api/v2.6/data?records={recordId}&records=...
   */

 @Builder.Factory
 public static HttpUrl getUrl(
     String scheme,
     String username,
     String domain,
     String namespace,
     String endpoint) {
  return new HttpUrl.Builder()
      .build();
 }

}
