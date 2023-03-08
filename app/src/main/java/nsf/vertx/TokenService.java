package nsf.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import nsf.pda.auth.AccessToken;

public class TokenService extends AbstractVerticle {

  private MessageProducer<AccessToken> producer;
  private AccessToken token;
  private Instant updatedAt;
  private long timeId;

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("auth.consumeToken", this::consumeToken);
    producer = vertx.eventBus().publisher("auth.publishToken");
    timeId = vertx.setPeriodic(0L, getTokenTtlMillis(), this::fetchAndPublishToken);
  }

  @Override
  public void stop() {
    vertx.cancelTimer(timeId);
  }

  private long getTokenTtlMillis() {
    JsonObject config = vertx.getOrCreateContext().config();
    long ttlSeconds = config.getLong("tokenTtlSeconds");
    return Math.multiplyExact(ttlSeconds, 1000L);
  }

  private void consumeToken(Message<AccessToken> message) {
    if (isMessageBasedOnCurrentToken(message)) {
      token = message.body();
    }
  }

  private boolean isMessageBasedOnCurrentToken(Message<AccessToken> message) {
    Instant createdAt = Instant.parse(message.headers().get("createdAt"));
    return !createdAt.isBefore(updatedAt);
  }

  private void fetchAndPublishToken(long timerId) {
    token = AccessToken.builder().build(); // TODO Fetch with HTTP client
    updatedAt = Instant.now();
    producer.write(token);
  }
}
