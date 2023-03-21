package nsf.vertx;

import io.vertx.ext.web.client.WebClient;

public interface WebStore {

  WebClient client();

  String host();
}
