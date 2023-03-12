package com.sp.reactive.boilerplate.security;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * @author sarvesh
 * @version 0.0.1
 * @since 0.0.1
 */
@Component
public class CognitoIdTokenProcessor {

  @Autowired
  private CognitoJwtConfigProperties cognitoJwtConfigProperties;

  @SuppressWarnings("rawtypes")
  @Autowired
  @Qualifier("CognitoConfigurableJWTProcessor")
  private ConfigurableJWTProcessor configurableJWTProcessor;

  public Authentication authenticate(ServerHttpRequest request) throws Exception {
    String token = request.getHeaders().getFirst(cognitoJwtConfigProperties.getHttpHeader());
    if (token != null) {
      token = token.replaceFirst("Bearer ", "");
      @SuppressWarnings("unchecked")
      JWTClaimsSet claims = configurableJWTProcessor.process(token, null);
      validateIssuer(claims);
      verifyIfIdToken(claims);
      String username = getUserNameFrom(claims);
      MDC.put("cognito_user", username);
      if (username != null) {
        List<String> groups = getUserGroupsFrom(claims);
        List<GrantedAuthority> grantedAuthorities = groups.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        User user = new User(username, "", grantedAuthorities);
        return new JwtAuthentication(user, claims, grantedAuthorities);
      }
    }
    return null;
  }

  private String getUserNameFrom(JWTClaimsSet claims) {
    return claims.getClaims().get(cognitoJwtConfigProperties.getUserNameField()).toString();
  }

  private List<String> getUserGroupsFrom(JWTClaimsSet claims) {
    try {
      return JSONObjectUtils.getStringList(claims.getClaims(),
          cognitoJwtConfigProperties.getUserGroupsField());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private void verifyIfIdToken(JWTClaimsSet claims) throws Exception {
    if (!claims.getIssuer().equals(cognitoJwtConfigProperties.getIdentityPoolUrl())) {
      throw new Exception("JWT Token is not an ID Token");
    }
  }

  private void validateIssuer(JWTClaimsSet claims) throws Exception {
    if (!claims.getIssuer().equals(cognitoJwtConfigProperties.getIdentityPoolUrl())) {
      throw new Exception(
          String.format("Issuer %s does not match cognito idp %s", claims.getIssuer(),
              cognitoJwtConfigProperties.getIdentityPoolUrl()));
    }
  }
}
