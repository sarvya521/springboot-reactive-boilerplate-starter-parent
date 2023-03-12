package com.sp.reactive.boilerplate.security;

import java.util.Objects;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public final class AuthUtil {

  private AuthUtil() {
    throw new AssertionError();
  }

  /**
   * Get the currently authenticated principal, or an authentication request token
   *
   * @return {@link Authentication}
   */
  public static Authentication getAuthentication() {
    Authentication authentication = ReactiveSecurityContextHolder.getContext().block()
        .getAuthentication();
    if (Objects.isNull(authentication)) {
      throw new AuthenticationServiceException("No Authentication object found in SecurityContext");
    }
    return authentication;
  }

  public static User getLoggedInUser() {
    JwtAuthentication authentication = (JwtAuthentication) getAuthentication();
    return (User) authentication.getPrincipal();
  }

  public static String getUsername() {
    return getLoggedInUser().getUsername();
  }
}
