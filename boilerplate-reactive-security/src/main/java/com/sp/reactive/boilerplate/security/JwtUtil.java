package com.sp.reactive.boilerplate.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import lombok.SneakyThrows;

public final class JwtUtil {

  private final RSAKey rsaKey;
  private final String issuer;
  private final int jwtExpiryInMs;

  public JwtUtil(String jwk, String issuer, int jwtExpiryInMs) throws ParseException {
    this.rsaKey = (RSAKey) JWK.parse(jwk);
    this.issuer = issuer;
    this.jwtExpiryInMs = jwtExpiryInMs;
  }

  @SneakyThrows(JOSEException.class)
  public String generateToken(UUID userId, String email, String name) {
    JWSSigner signer = new RSASSASigner(rsaKey);
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .issuer(issuer)
        .expirationTime(new Date(new Date().getTime() + jwtExpiryInMs))
        .claim("email", email)
        .claim("name", name)
        .build();
    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS512).keyID(rsaKey.getKeyID()).type(JOSEObjectType.JWT)
            .build(),
        claimsSet);
    signedJWT.sign(signer);
    return signedJWT.serialize();
  }

  String getUserNameFrom(JWTClaimsSet claims) {
    return claims.getClaims().get("sub").toString();
  }

  void validateIssuer(JWTClaimsSet claims) {
    if (!claims.getIssuer().equals(issuer)) {
      throw new TokenException(new ErrorDetails());
    }
  }

}
