package vertx;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class Tokens {


  private Tokens() {
  }

  public static String host() {
    return "test.host.com";
  }

  public static String valid(Instant iat, Instant exp) {
    return build(iat, exp);
  }

  public static String missingIssClaim(Instant iat, Instant exp) {
    return build(iat, exp, builder -> builder.setIssuer(null));
  }

  public static String missingIatClaim(Instant exp) {
    return build(null, exp);
  }

  public static String missingExpClaim(Instant iat) {
    return build(iat, null);
  }

  public static String missingResourceClaim(Instant iat, Instant exp) {
    return build(iat, exp, builder -> builder.claim("resource", null));
  }

  public static String missingAccessScopeClaim(Instant iat, Instant exp) {
    return build(iat, exp, builder -> builder.claim("accessScope", null));
  }

  public static String wrongPrivateKey(Instant iat, Instant exp) {
    return build(iat, exp, builder -> builder.signWith(KeyPairs.incorrectPrivate()));
  }

  public static String empty() {
    return "";
  }

  private static String build(Instant iat, Instant exp) {
    return build(iat, exp, UnaryOperator.identity());
  }

  private static String build(Instant iat, Instant exp, UnaryOperator<JwtBuilder> modifier) {
    JwtBuilder builder = Jwts.builder()
        .setIssuer(host())
        .setIssuedAt(toDateOrNull(iat))
        .setExpiration(toDateOrNull(exp))
        .claim("resource", host())
        .claim("accessScope", "owner")
        .signWith(KeyPairs.correctPrivate());
    return modifier.apply(builder).compact();
  }

  private static Date toDateOrNull(Instant instant) {
    return Optional.ofNullable(instant).map(Date::from).orElse(null);
  }
}
