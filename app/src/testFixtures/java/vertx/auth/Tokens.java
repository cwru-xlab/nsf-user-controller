package vertx.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import nsf.vertx.auth.token.DecodedTokenBuilder;
import nsf.vertx.auth.token.EncodedTokenBuilder;
import nsf.vertx.auth.token.Token;

public final class Tokens {

  private Tokens() {
  }

  public static String host() {
    return "test.host.com";
  }

  public static Duration timeToLive() {
    return Duration.ofDays(2);
  }

  public static Instant issuedAt() {
    return Instant.EPOCH;
  }

  public static Instant expiresAt() {
    return issuedAt().plus(timeToLive());
  }

  public static Instant now() {
    return issuedAt().plus(timeToLive().dividedBy(2));
  }

  public static Instant afterExpiration() {
    return expiresAt().plus(Duration.ofMillis(1));
  }

  public static Token valid(Instant issuedAt, Instant expiresAt) {
    return Token.of(encodedValid(issuedAt, expiresAt), decodedValid(issuedAt, expiresAt));
  }

  public static String encodedValid(Instant issuedAt, Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .addIssuer(true)
        .addResource(true)
        .addAccessScope(true)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedValid(Instant issuedAt, Instant expiresAt) {
    return decode(encodedValid(issuedAt, expiresAt), issuedAt);
  }

  public static Token missingIssuerClaim(Instant issuedAt, Instant expiresAt) {
    return Token.builder()
        .encoded(encodedMissingIssuerClaim(issuedAt, expiresAt))
        .decoded(decodedMissingIssuerClaim(issuedAt, expiresAt))
        .build();
  }

  public static String encodedMissingIssuerClaim(Instant issuedAt, Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .addIssuer(false)
        .addResource(true)
        .addAccessScope(true)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedMissingIssuerClaim(Instant issuedAt, Instant expiresAt) {
    return decode(encodedMissingIssuerClaim(issuedAt, expiresAt), issuedAt);
  }

  public static Token missingIssuedAtClaim(Instant expiresAt) {
    return Token.of(encodedMissingIssuedAtClaim(expiresAt), decodedMissingIssuedAtClaim(expiresAt));
  }

  public static String encodedMissingIssuedAtClaim(Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .expiresAt(expiresAt)
        .addIssuer(true)
        .addResource(true)
        .addAccessScope(true)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedMissingIssuedAtClaim(Instant expiresAt) {
    return decode(encodedMissingIssuedAtClaim(expiresAt), expiresAt);
  }

  public static Token missingExpiresAtClaim(Instant issuedAt) {
    return Token.of(encodedMissingExpiresAt(issuedAt), decodedMissingExpiresAt(issuedAt));
  }

  public static String encodedMissingExpiresAt(Instant issuedAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .addIssuer(true)
        .addResource(true)
        .addAccessScope(true)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedMissingExpiresAt(Instant issuedAt) {
    return decode(encodedMissingExpiresAt(issuedAt), issuedAt);
  }

  public static Token missingResourceClaim(Instant issuedAt, Instant expiresAt) {
    return Token.builder()
        .encoded(encodedMissingResourceClaim(issuedAt, expiresAt))
        .decoded(decodedMissingResourceClaim(issuedAt, expiresAt))
        .build();
  }

  public static String encodedMissingResourceClaim(Instant issuedAt, Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .addIssuer(true)
        .addResource(false)
        .addAccessScope(true)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedMissingResourceClaim(Instant issuedAt, Instant expiresAt) {
    return decode(encodedMissingResourceClaim(issuedAt, expiresAt), issuedAt);
  }

  public static Token missingAccessScopeClaim(Instant issuedAt, Instant expiresAt) {
    return Token.builder()
        .encoded(encodedMissingAccessScopeClaim(issuedAt, expiresAt))
        .decoded(decodedMissingAccessScopeClaim(issuedAt, expiresAt))
        .build();
  }

  public static String encodedMissingAccessScopeClaim(Instant issuedAt, Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .addIssuer(true)
        .addResource(true)
        .addAccessScope(false)
        .privateKey(KeyPairs.correctPrivate())
        .build();
  }

  public static Jws<Claims> decodedMissingAccessScopeClaim(Instant issuedAt, Instant expiresAt) {
    return decode(encodedMissingAccessScopeClaim(issuedAt, expiresAt), issuedAt);
  }

  public static Token incorrectPrivateKey(Instant issuedAt, Instant expiresAt) {
    return Token.builder()
        .encoded(encodedIncorrectPrivateKey(issuedAt, expiresAt))
        .decoded(decodedIncorrectPrivateKey(issuedAt, expiresAt))
        .build();
  }

  public static String encodedIncorrectPrivateKey(Instant issuedAt, Instant expiresAt) {
    return EncodedTokenBuilder.create()
        .host(host())
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .addIssuer(true)
        .addResource(true)
        .addAccessScope(true)
        .privateKey(KeyPairs.incorrectPrivate())
        .build();
  }

  public static Jws<Claims> decodedIncorrectPrivateKey(Instant issuedAt, Instant expiresAt) {
    return decode(encodedIncorrectPrivateKey(issuedAt, expiresAt), issuedAt);
  }

  public static String encodedEmpty() {
    return "";
  }

  private static Jws<Claims> decode(String encoded, Instant clockTime) {
    return DecodedTokenBuilder.create()
        .encoded(encoded)
        .host(host())
        .publicKey(KeyPairs.correctPublic())
        .clock(Clock.fixed(clockTime, ZoneOffset.UTC))
        .strict(false)
        .build();
  }
}
