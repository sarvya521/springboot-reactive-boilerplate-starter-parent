package com.sp.reactive.boilerplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.reactive.boilerplate.commons.constant.Status;
import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import com.sp.reactive.boilerplate.commons.dto.Response;
import com.sp.reactive.boilerplate.commons.exception.BoilerplateException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class BoilerplateSecurityAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  static final String ERROR_MESSAGE = "Authorization token not present or invalid";
  static final String EXPIRED_TOKEN_ERROR_MESSAGE = "Authorization token is expired";

  private ObjectMapper objectMapper;

  public BoilerplateSecurityAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  @SneakyThrows
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
    log.error("Unauthenticated Access", authException);
    ServerHttpResponse res = exchange.getResponse();
    if (res.getStatusCode() == HttpStatus.FORBIDDEN) {
      return Mono.empty();
    }

    res.setStatusCode(HttpStatus.UNAUTHORIZED);
    res.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    Response<?> response = new Response<>();
    response.setStatus(Status.FAIL);
    response.setCode(HttpStatus.UNAUTHORIZED.value());
    ErrorDetails errorDetails;
    if (authException.getCause() instanceof BoilerplateException) {
      errorDetails = ((BoilerplateException) authException.getCause()).getError();
    } else {
      errorDetails = new ErrorDetails(String.valueOf(HttpStatus.UNAUTHORIZED.value()),
          authException.getMessage());
    }
    List<ErrorDetails> errors = new ArrayList<>();
    errors.add(errorDetails);
    response.setErrors(errors);

    byte[] bytes = objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = res.bufferFactory().wrap(bytes);
    return res.writeWith(Mono.just(buffer));
  }
}
