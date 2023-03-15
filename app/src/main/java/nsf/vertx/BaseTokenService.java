package nsf.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicLong;
import org.immutables.value.Value;

@Value.Immutable
@SuppressWarnings("immutables:incompat")
abstract class BaseTokenService extends AbstractVerticle {

  private static final long UNSET_TIMER_ID = -1;

  protected abstract PublicKeyStore keyStore();

  protected abstract TokenStore tokenStore();

  @Value.Derived
  protected MessageProducer<String> publisher() {
    return getVertx().eventBus().publisher("auth.publishToken");
  }

  @Value.Derived
  protected AtomicLong timerId() {
    return new AtomicLong(UNSET_TIMER_ID);
  }

  @Override
  public void start(Promise<Void> promise) {
    keyStore().get()
        .flatMap(this::getAndPublishToken)
        .onSuccess(promise::complete)
        .onFailure(promise::fail);
  }

  @Override
  public void stop() {
    long timerId = timerId().get();
    getVertx().cancelTimer(timerId);
    timerId().set(UNSET_TIMER_ID);
  }

  private Future<Void> getAndPublishToken(PublicKey publicKey) {
    return tokenStore().get(publicKey)
        .onSuccess(token -> startPeriodRefresh(token, publicKey))
        .map(Token::encoded)
        .flatMap(publisher()::write)
        .mapEmpty();
  }

  private void startPeriodRefresh(Token token, PublicKey publicKey) {
    if (isTimerIdNotSet()) {
      long safeTtl = Math.round(token.ttl().toMillis() * 0.95);
      long timerId = getVertx().setPeriodic(safeTtl, x -> getAndPublishToken(publicKey));
      timerId().set(timerId);
    }
  }

  private boolean isTimerIdNotSet() {
    return timerId().get() < 0;
  }
}
