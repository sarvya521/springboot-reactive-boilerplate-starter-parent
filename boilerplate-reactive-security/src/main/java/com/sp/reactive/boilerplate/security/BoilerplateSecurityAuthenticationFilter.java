package com.sp.reactive.boilerplate.security;

import com.sp.reactive.boilerplate.commons.exception.BoilerplateException;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class BoilerplateSecurityAuthenticationFilter implements WebFilter {

  private static final String COGNITO_AUTH_HEADER = "x-aws-cognito-auth";
  private static final String MS_AUTH_HEADER = "x-ms-auth";
  private static final String GUEST_AUTH_HEADER = "x-guest-auth";
  private static final Set<String> BOOLEAN_VALUES = Set.of("true", "yes", "y", "1");

  private final CognitoIdTokenProcessor cognitoIdTokenProcessor;
  private final MicrosoftTokenProcessor microsoftTokenProcessor;
  private final GuestTokenProcessor guestTokenProcessor;
  private final ServerAuthenticationEntryPoint authenticationEntryPoint;

  public BoilerplateSecurityAuthenticationFilter(CognitoIdTokenProcessor cognitoIdTokenProcessor,
      MicrosoftTokenProcessor microsoftTokenProcessor, GuestTokenProcessor guestTokenProcessor,
      ServerAuthenticationEntryPoint authenticationEntryPoint) {
    this.cognitoIdTokenProcessor = cognitoIdTokenProcessor;
    this.microsoftTokenProcessor = microsoftTokenProcessor;
    this.guestTokenProcessor = guestTokenProcessor;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  /**
   * {@inheritDoc}
   */
  @SneakyThrows
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    Authentication authentication = null;
    ServerHttpRequest request = exchange.getRequest();
    try {
      String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (!StringUtils.hasText(token)) {
        return authenticationEntryPoint.commence(exchange,
            new AuthenticationServiceException(
                BoilerplateSecurityAuthenticationEntryPoint.ERROR_MESSAGE));
      }

      String cognitoAuthHeader = request.getHeaders().getFirst(COGNITO_AUTH_HEADER);
      String msAuthHeader = request.getHeaders().getFirst(MS_AUTH_HEADER);
      String guestAuthHeader = request.getHeaders().getFirst(GUEST_AUTH_HEADER);

      if (StringUtils.hasText(cognitoAuthHeader) && BOOLEAN_VALUES.contains(
          cognitoAuthHeader.toLowerCase())) {
        authentication = cognitoIdTokenProcessor.authenticate(request);
      } else if (StringUtils.hasText(msAuthHeader) && BOOLEAN_VALUES.contains(
          msAuthHeader.toLowerCase())) {
        authentication = microsoftTokenProcessor.authenticate(request);
      } else if (StringUtils.hasText(guestAuthHeader) && BOOLEAN_VALUES.contains(
          guestAuthHeader.toLowerCase())) {
        authentication = guestTokenProcessor.authenticate(request);
      }
    } catch (BoilerplateException pe) {
      return authenticationEntryPoint.commence(exchange,
          new AuthenticationServiceException(pe.getMessage(), pe));
    } catch (Exception e) {
      log.error("Auth Unknown Error", e);
      return authenticationEntryPoint.commence(exchange,
          new AuthenticationServiceException(
              BoilerplateSecurityAuthenticationEntryPoint.ERROR_MESSAGE));
    }
    return chain.filter(exchange)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
  }
}
