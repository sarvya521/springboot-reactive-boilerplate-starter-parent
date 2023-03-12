package com.sp.reactive.boilerplate.security;

import static com.sp.reactive.boilerplate.security.BoilerplateSecurityCorsConfiguration.ALLOWED_HEADERS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class BoilerplateSecurityCorsProcessor extends DefaultCorsProcessor {

  private final List<String> allowedOrigins;

  public BoilerplateSecurityCorsProcessor(String allowedUrls) {
    allowedOrigins = Optional.ofNullable(allowedUrls)
        .map(urls -> Arrays.stream(urls.split(",")).collect(Collectors.toList())).orElse(
            Collections.emptyList());
    log.info("Configured origins {}", allowedOrigins);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean process(@Nullable CorsConfiguration config, ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    ServerHttpResponse response = exchange.getResponse();
    String origin = request.getHeaders().getOrigin();
    boolean hasAllowedOrigin = Optional.ofNullable(origin)
        .map(headerValue -> allowedOrigins.stream()
            .anyMatch(allowedOrigin -> allowedOrigin.equals(headerValue)))
        .orElse(false);

    if (hasAllowedOrigin || allowedOrigins.contains("*")) {
      response.getHeaders().add("Access-Control-Allow-Origin", origin);
    } else {
      log.trace("request url {}", request.getURI());
    }
    response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.getHeaders().add("Access-Control-Max-Age", "3600");
    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
    response.getHeaders().add("Access-Control-Allow-Headers", String.join(",", ALLOWED_HEADERS));
    response.getHeaders().add("Access-Control-Expose-Headers", "x-trace-id");
    return super.process(config, exchange);
  }
}
