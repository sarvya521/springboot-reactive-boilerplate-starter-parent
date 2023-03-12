package com.sp.reactive.boilerplate.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author sarvesh
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Component
public class CognitoJwtConfigProperties {

  private final String region;

  private final String userPoolId;

  private final String jwkUrl;

  private final String identityPoolUrl;

  private final String httpHeader = "Authorization";

  private final String userNameField = "cognito:username";

  private final String userGroupsField = "cognito:groups";

  private final int connectionTimeout = 2000;

  private final int readTimeout = 2000;

  public CognitoJwtConfigProperties(@Value("${aws.cognito.region}") String region,
      @Value("${aws.cognito.userPoolId}") String userPoolId) {
    this.region = region;
    this.userPoolId = userPoolId;
    this.jwkUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
        this.region, this.userPoolId);
    this.identityPoolUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s", this.region,
        this.userPoolId);
  }
}
