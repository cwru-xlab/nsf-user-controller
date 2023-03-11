package nsf.vertx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TokenService extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
  private static final long UNSET_TIMER_ID = -1;

  private final MessageProducer<String> publisher;
  private final WebClient client;
  private final String host;
  private final Clock clock;

  private byte[] publicKey;
  private long timerId;

  public TokenService(WebClient client, String host, Clock clock) {
    this.client = client;
    this.host = host;
    this.clock = clock;
    this.publisher = vertx.eventBus().publisher("auth.publishToken");
    this.timerId = UNSET_TIMER_ID;
  }

  @Override
  public void start(Promise<Void> promise) {
    fetchAndSetPublicKey()
        .andThen(x -> fetchAndPublishToken())
        .onSuccess(promise::complete)
        .onFailure(promise::fail);
  }

  @Override
  public void stop() {
    vertx.cancelTimer(timerId);
    timerId = UNSET_TIMER_ID;
  }

  private Future<Void> fetchAndSetPublicKey() {
    return client
        .get(host, "/publickey")
        .send()
        .onSuccess(this::setPublicKey)
        .onFailure(this::logPublicKeyFetchFailure)
        .mapEmpty();
  }

  private void setPublicKey(HttpResponse<Buffer> response) {
    publicKey = response.body().getBytes();
  }

  private void logPublicKeyFetchFailure(Throwable throwable) {
    logIfErrorEnabled(() -> "Unable to fetch public key for host " + host, throwable);
  }

  private void fetchAndPublishToken() {
    client
        .get(host, "/access_token")
        .as(BodyCodec.jsonObject())
        .send()
        .onSuccess(this::verifyAndPublishToken)
        .onFailure(this::logTokenFetchFailure);
  }

  private void verifyAndPublishToken(HttpResponse<JsonObject> response) {
    String token = response.body().getString("accessToken");
    verifyToken(token)
        .onSuccess(this::startPeriodRefresh)
        .onFailure(this::logTokenVerificationFailure)
        .andThen(x -> publisher.write(token));
  }

  @SuppressWarnings("rawtypes")
  private Future<Jwt<Header, Claims>> verifyToken(String token) {
    try {
      return Future.succeededFuture(
          Jwts.parserBuilder()
              .requireIssuer(host)
              .require("accessScope", "owner")
              .setClock(this::now)
              .setSigningKey(publicKey)
              .build()
              .parseClaimsJwt(token));
    } catch (UnsupportedJwtException | IllegalArgumentException | MalformedJwtException |
             SignatureException | ExpiredJwtException exception) {
      return Future.failedFuture(exception);
    }
  }

  private Date now() {
    return Date.from(clock.instant());
  }

  @SuppressWarnings("rawtypes")
  private void startPeriodRefresh(Jwt<Header, Claims> jwt) {
    if (isTimerIdNotSet()) {
      Instant iat = jwt.getBody().getIssuedAt().toInstant();
      Instant exp = jwt.getBody().getExpiration().toInstant();
      long exactTtl = iat.until(exp, ChronoUnit.MILLIS);
      long safeTtl = Math.round(exactTtl * 0.95);
      timerId = vertx.setPeriodic(safeTtl, x -> fetchAndPublishToken());
    }
  }

  private boolean isTimerIdNotSet() {
    return timerId < 0;
  }

  private void logTokenVerificationFailure(Throwable throwable) {
    logIfErrorEnabled(throwable::getMessage, throwable);
  }

  private void logTokenFetchFailure(Throwable throwable) {
    logIfErrorEnabled(() -> "Unable to fetch access token for host " + host, throwable);
  }

  private void logIfErrorEnabled(Supplier<String> message, Throwable throwable) {
    if (logger.isErrorEnabled()) {
      logger.error(message.get(), throwable);
    }
  }
}
