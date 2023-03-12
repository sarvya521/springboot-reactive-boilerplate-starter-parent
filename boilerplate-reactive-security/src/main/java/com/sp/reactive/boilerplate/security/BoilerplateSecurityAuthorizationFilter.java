package com.sp.reactive.boilerplate.security;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class BoilerplateSecurityAuthorizationFilter implements WebFilter {

  private final ServerAccessDeniedHandler accessDeniedHandler;

  private final ServerAuthenticationEntryPoint authenticationEntryPoint;

  public BoilerplateSecurityAuthorizationFilter(final ServerAccessDeniedHandler accessDeniedHandler,
      final ServerAuthenticationEntryPoint authenticationEntryPoint) {
    this.accessDeniedHandler = accessDeniedHandler;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    if (Objects.isNull(AuthUtil.getAuthentication())) {
      return authenticationEntryPoint.commence(exchange, new AuthenticationServiceException(
          BoilerplateSecurityAuthenticationEntryPoint.ERROR_MESSAGE));
    }
    log.debug("authorizing user access");
    try {
      Authorizer.doAuthorize(request, AuthUtil.getLoggedInUser());
    } catch (AccessDeniedException e) {
      return accessDeniedHandler.handle(exchange, e);
    }
    return chain.filter(exchange);
  }
}
