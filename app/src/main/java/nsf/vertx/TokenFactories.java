package nsf.vertx;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.immutables.builder.Builder;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
final class TokenFactories {

  @Builder.Factory
  static JwtParser tokenParser(String host, Key signingKey, Clock clock) {
    return Jwts.parserBuilder()
        .requireIssuer(host)
        .require("resource", host)
        .require("accessScope", "owner")
        .setClock(() -> Date.from(clock.instant()))
        .setSigningKey(signingKey)
        .build();
  }

  @Builder.Factory
  static String encodedToken(
      PrivateKey privateKey,
      Optional<String> iss,
      Optional<Instant> iat,
      Optional<Instant> exp,
      Optional<String> resource,
      Optional<Boolean> addAccessScope) {
    return Jwts.builder()
        .setIssuer(iss.orElse(null))
        .signWith(privateKey)
        .setIssuedAt(toDateOrNull(iat))
        .setExpiration(toDateOrNull(exp))
        .claim("resource", resource.orElse(null))
        .claim("accessScope", addAccessScope.orElse(false) ? "owner" : null)
        .compact();
  }

  private static Date toDateOrNull(Optional<Instant> instant) {
    return instant.map(Date::from).orElse(null);
  }
}
