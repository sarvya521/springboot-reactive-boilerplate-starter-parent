package com.sp.reactive.boilerplate.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

public class BoilerplateSecurityCorsConfiguration extends UrlBasedCorsConfigurationSource {

  static final List<String> ALLOWED_HEADERS = Arrays.asList(
      "x-trace-id", "Accept", "Accept-Language", "Content-Language",
      "Content-Type", "Authorization", "Origin", "Referer", "Cookie",
      "x-aws-cognito-auth", "x-ms-auth", "x-guest-auth");

  private final List<String> allowedOrigins;

  public BoilerplateSecurityCorsConfiguration(String allowedUrls) {
    allowedOrigins = Optional.ofNullable(allowedUrls)
        .map(urls -> Arrays.stream(urls.split(",")).collect(Collectors.toList())).orElse(
            Collections.emptyList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerCorsConfiguration(String path, CorsConfiguration configuration) {
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    configuration.setMaxAge(3600L);
    configuration.setAllowCredentials(true);
    configuration.setAllowedHeaders(ALLOWED_HEADERS);

    super.registerCorsConfiguration(path, configuration);
  }
}
