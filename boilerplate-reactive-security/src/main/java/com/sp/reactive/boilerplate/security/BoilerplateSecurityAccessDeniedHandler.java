package com.sp.reactive.boilerplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.reactive.boilerplate.commons.constant.Status;
import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import com.sp.reactive.boilerplate.commons.dto.Response;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class BoilerplateSecurityAccessDeniedHandler implements ServerAccessDeniedHandler {

  private static final String ERROR_MESSAGE = "You are not allowed to access this resource";

  private ObjectMapper objectMapper;

  public BoilerplateSecurityAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  @SneakyThrows
  @SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
  @Override
  public Mono<Void> handle(ServerWebExchange exchange,
      AccessDeniedException accessDeniedException) {
    log.error("user is not allowed to access the resource", accessDeniedException);
    ReactiveSecurityContextHolder.clearContext();

    ServerHttpResponse res = exchange.getResponse();

    res.setStatusCode(HttpStatus.FORBIDDEN);
    res.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    Response<?> response = new Response<>();
    response.setStatus(Status.FAIL);
    response.setCode(HttpStatus.FORBIDDEN.value());
    ErrorDetails errorDetails = new ErrorDetails(String.valueOf(HttpStatus.FORBIDDEN.value()),
        ERROR_MESSAGE);
    List<ErrorDetails> errors = new ArrayList<>();
    errors.add(errorDetails);
    response.setErrors(errors);
    byte[] bytes = objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = res.bufferFactory().wrap(bytes);
    return res.writeWith(Mono.just(buffer));
  }
}
