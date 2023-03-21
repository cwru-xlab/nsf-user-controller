package nsf.vertx.auth.token;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.immutables.builder.Builder;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
final class TokenFactories {

  private static final Map.Entry<String, String> ACCESS_SCOPE = Map.entry("accessScope", "owner");
  private static final String RESOURCE_KEY = "resource";

  @Builder.Factory
  public static Jws<Claims> decodedToken(
      String encoded,
      String host,
      PublicKey publicKey,
      Clock clock,
      Optional<Boolean> strict) {
    return strict.orElse(false)
        ? strictDecode(encoded, host, publicKey, clock)
        : decode(encoded, publicKey, clock);
  }

  private static Jws<Claims> decode(String encoded, PublicKey publicKey, Clock clock) {
    return Jwts.parserBuilder()
        .setClock(() -> Date.from(clock.instant()))
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(encoded);
  }

  private static Jws<Claims> strictDecode(
      String encoded, String host, PublicKey publicKey, Clock clock) {
    Jws<Claims> decoded = Jwts.parserBuilder()
        .requireIssuer(host)
        .require(RESOURCE_KEY, host)
        .require(ACCESS_SCOPE.getKey(), ACCESS_SCOPE.getValue())
        .setClock(() -> Date.from(clock.instant()))
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(encoded);
    requireClaim(decoded, Claims::getIssuedAt, Claims.ISSUED_AT);
    requireClaim(decoded, Claims::getExpiration, Claims.EXPIRATION);
    return decoded;
  }

  private static void requireClaim(Jws<Claims> token, Function<Claims, ?> getClaim, String name) {
    Optional.of(token)
        .map(Jws::getBody)
        .map(getClaim)
        .orElseThrow(() -> missingClaimException(token, name));
  }

  private static MissingClaimException missingClaimException(Jws<Claims> token, String name) {
    String template = ClaimJwtException.MISSING_EXPECTED_CLAIM_MESSAGE_TEMPLATE;
    String message = String.format(template, name, "non-null");
    return new MissingClaimException(token.getHeader(), token.getBody(), message);
  }

  @Builder.Factory
  public static String encodedToken(
      String host,
      PrivateKey privateKey,
      Optional<Instant> issuedAt,
      Optional<Instant> expiresAt,
      Optional<Boolean> addIssuer,
      Optional<Boolean> addResource,
      Optional<Boolean> addAccessScope) {
    return Jwts.builder()
        .setIssuer(ifTrueElseNull(addIssuer, host))
        .setIssuedAt(toDateElseNull(issuedAt))
        .setExpiration(toDateElseNull(expiresAt))
        .claim(RESOURCE_KEY, ifTrueElseNull(addResource, host))
        .claim(ACCESS_SCOPE.getKey(), ifTrueElseNull(addAccessScope, ACCESS_SCOPE.getValue()))
        .signWith(privateKey)
        .compact();
  }

  private static <T> T ifTrueElseNull(Optional<Boolean> optional, T ifTrue) {
    return optional.orElse(false) ? ifTrue : null;
  }

  private static Date toDateElseNull(Optional<Instant> instant) {
    return instant.map(Date::from).orElse(null);
  }
}
