package vertx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import nsf.vertx.EncodedTokenBuilder;
import nsf.vertx.Token;
import nsf.vertx.TokenParserBuilder;

public final class Tokens {

  private Tokens() {
  }

  public static String host() {
    return "test.host.com";
  }

  public static Token valid(Instant iat, Instant exp) {
    return Token.of(encodedValid(iat, exp), decodedValid(iat, exp));
  }

  public static String encodedValid(Instant iat, Instant exp) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .iat(iat)
        .exp(exp)
        .privateKey(KeyPairs.correctPrivate())
        .resource(host())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedValid(Instant iat, Instant exp) {
    return decode(encodedValid(iat, exp), iat);
  }

  public static Token missingIssClaim(Instant iat, Instant exp) {
    return Token.of(encodedMissingIssClaim(iat, exp), decodedMissingIssClaim(iat, exp));
  }

  public static String encodedMissingIssClaim(Instant iat, Instant exp) {
    return EncodedTokenBuilder.create()
        .iat(iat)
        .exp(exp)
        .privateKey(KeyPairs.correctPrivate())
        .resource(host())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedMissingIssClaim(Instant iat, Instant exp) {
    return decode(encodedMissingIssClaim(iat, exp), iat);
  }

  public static Token missingIatClaim(Instant exp) {
    return Token.of(encodedMissingIatClaim(exp), decodedMissingIatClaim(exp));
  }

  public static String encodedMissingIatClaim(Instant exp) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .exp(exp)
        .privateKey(KeyPairs.correctPrivate())
        .resource(host())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedMissingIatClaim(Instant exp) {
    return decode(encodedMissingIatClaim(exp), exp);
  }

  public static Token missingExpClaim(Instant iat) {
    return Token.of(encodedMissingExpClaim(iat), decodedMissingExpClaim(iat));
  }

  public static String encodedMissingExpClaim(Instant iat) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .iat(iat)
        .privateKey(KeyPairs.correctPrivate())
        .resource(host())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedMissingExpClaim(Instant iat) {
    return decode(encodedMissingExpClaim(iat), iat);
  }

  public static Token missingResourceClaim(Instant iat, Instant exp) {
    return Token.of(encodedMissingResourceClaim(iat, exp), decodedMissingResourceClaim(iat, exp));
  }

  public static String encodedMissingResourceClaim(Instant iat, Instant exp) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .iat(iat)
        .exp(exp)
        .privateKey(KeyPairs.correctPrivate())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedMissingResourceClaim(Instant iat, Instant exp) {
    return decode(encodedMissingResourceClaim(iat, exp), iat);
  }

  public static Token missingAccessScopeClaim(Instant iat, Instant exp) {
    return Token.of(
        encodedMissingAccessScopeClaim(iat, exp),
        decodedMissingAccessScopeClaim(iat, exp));
  }

  public static String encodedMissingAccessScopeClaim(Instant iat, Instant exp) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .iat(iat)
        .exp(exp)
        .privateKey(KeyPairs.correctPrivate())
        .resource(host())
        .build();
  }

  public static Jws<Claims> decodedMissingAccessScopeClaim(Instant iat, Instant exp) {
    return decode(encodedMissingAccessScopeClaim(iat, exp), iat);
  }

  public static Token wrongPrivateKey(Instant iat, Instant exp) {
    return Token.of(encodedWrongPrivateKey(iat, exp), decodedWrongPrivateKey(iat, exp));
  }

  public static String encodedWrongPrivateKey(Instant iat, Instant exp) {
    return EncodedTokenBuilder.create()
        .iss(host())
        .iat(iat)
        .exp(exp)
        .privateKey(KeyPairs.incorrectPrivate())
        .resource(host())
        .addAccessScope(true)
        .build();
  }

  public static Jws<Claims> decodedWrongPrivateKey(Instant iat, Instant exp) {
    return decode(encodedWrongPrivateKey(iat, exp), iat);
  }

  public static String encodedEmpty() {
    return "";
  }

  private static Jws<Claims> decode(String encoded, Instant clockTime) {
    return TokenParserBuilder.create()
        .host(host())
        .signingKey(KeyPairs.correctPublic())
        .clock(Clock.fixed(clockTime, ZoneOffset.UTC))
        .build()
        .parseClaimsJws(encoded);
  }
}
