package com.sp.reactive.boilerplate.security;

import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static com.nimbusds.jose.JWSAlgorithm.RS512;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.URL;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(WebProperties.class)
public class BoilerplateSecurityAutoConfiguration {

  @Autowired
  private Environment env;

  @Bean("AuthObjectMapper")
  public ObjectMapper authObjectMapper() {
    return new ObjectMapper();
  }

  @Bean("BoilerplateSecurityAccessDeniedHandler")
  public ServerAccessDeniedHandler ocAccessDeniedHandler(
      @Qualifier("AuthObjectMapper") ObjectMapper authObjectMapper) {
    log.debug("configuring BoilerplateSecurityAccessDeniedHandler");
    return new BoilerplateSecurityAccessDeniedHandler(authObjectMapper);
  }

  @Bean("BoilerplateSecurityAuthenticationEntryPoint")
  public ServerAuthenticationEntryPoint ocAuthenticationEntryPoint(
      @Qualifier("AuthObjectMapper") ObjectMapper authObjectMapper) {
    log.debug("configuring BoilerplateSecurityAuthenticationEntryPoint");
    return new BoilerplateSecurityAuthenticationEntryPoint(authObjectMapper);
  }

  @Bean("BoilerplateSecurityCorsFilter")
  public CorsWebFilter corsFilter(
      @Qualifier("BoilerplateSecurityCorsConfiguration") CorsConfigurationSource corsConfigurationSource) {
    log.debug("configuring BoilerplateSecurityCorsFilter");
    String allowedUrls = env.getProperty("boilerplate.cors.allowed-origins");
    Objects.requireNonNull(allowedUrls,
        "property {boilerplate.cors.allowed-origins} is not configured");
    return new CorsWebFilter(corsConfigurationSource,
        new BoilerplateSecurityCorsProcessor(allowedUrls));
  }

  @Bean("BoilerplateSecurityCorsConfiguration")
  public CorsConfigurationSource corsConfigurationSource() {
    String allowedUrls = env.getProperty("boilerplate.cors.allowed-origins");
    Objects.requireNonNull(allowedUrls,
        "property {boilerplate.cors.allowed-origins} is not configured");
    BoilerplateSecurityCorsConfiguration corsConfiguration = new BoilerplateSecurityCorsConfiguration(
        allowedUrls);
    corsConfiguration.registerCorsConfiguration("/**", new CorsConfiguration());
    return corsConfiguration;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @SneakyThrows
  @Bean("CognitoConfigurableJWTProcessor")
  public ConfigurableJWTProcessor cognitoConfigurableJWTProcessor(
      CognitoJwtConfigProperties cognitoJwtConfigProperties) {
    ResourceRetriever resourceRetriever =
        new DefaultResourceRetriever(cognitoJwtConfigProperties.getConnectionTimeout(),
            cognitoJwtConfigProperties.getReadTimeout());
    URL jwkURL = new URL(cognitoJwtConfigProperties.getJwkUrl());
    JWKSource keySource = new RemoteJWKSet(jwkURL, resourceRetriever);
    ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
    JWSKeySelector keySelector = new JWSVerificationKeySelector(RS256, keySource);
    jwtProcessor.setJWSKeySelector(keySelector);
    return jwtProcessor;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @SneakyThrows
  @Bean("MicrosoftConfigurableJWTProcessor")
  public ConfigurableJWTProcessor microsoftConfigurableJWTProcessor() {
    String jwkUrl = env.getProperty("boilerplate.ms.jwks");
    Objects.requireNonNull(jwkUrl,
        "property {boilerplate.ms.jwks} is not configured");
    JWKSet jwkSet = JWKSet.load(new URL(jwkUrl));
    JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(jwkSet);
    ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
    jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt")));
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<SecurityContext>(RS256, keySource);
    jwtProcessor.setJWSKeySelector(keySelector);
    return jwtProcessor;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @SneakyThrows
  @Bean("GuestConfigurableJWTProcessor")
  public ConfigurableJWTProcessor guestConfigurableJWTProcessor() {
    String jwk = env.getProperty("boilerplate.guest.jwk");
    Objects.requireNonNull(jwk,
        "property {boilerplate.guest.jwk} is not configured");
    JWKSet jwkSet = new JWKSet(JWK.parse(jwk));
    JWKSource<SecurityContext> keySource = new ImmutableJWKSet<>(jwkSet);
    ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
    jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt")));
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<>(RS512, keySource);
    jwtProcessor.setJWSKeySelector(keySelector);
    return jwtProcessor;
  }

  @Bean("GuestJwtUtil")
  @SneakyThrows
  public JwtUtil jwtUtil() {
    String jwk = env.getProperty("boilerplate.guest.jwk");
    Objects.requireNonNull(jwk,
        "property {boilerplate.guest.jwk} is not configured");
    String issuer = env.getProperty("boilerplate.guest.issuer");
    Objects.requireNonNull(issuer,
        "property {boilerplate.guest.issuer} is not configured");
    String expiryInMs = env.getProperty("boilerplate.guest.expiryInMs");
    Objects.requireNonNull(expiryInMs,
        "property {boilerplate.guest.expiryInMs} is not configured");
    return new JwtUtil(jwk, issuer, Integer.parseInt(expiryInMs));
  }


  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  public SecurityWebFilterChain whitelistOptions(ServerHttpSecurity http, Tracer tracer) {
    return http.securityMatcher(
            new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.OPTIONS))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 2)
  public SecurityWebFilterChain whitelistBoilerplateLogin(ServerHttpSecurity http, Tracer tracer) {
    return http.securityMatcher(
            new PathPatternParserServerWebExchangeMatcher("/v1/auth/login", HttpMethod.POST))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 3)
  public SecurityWebFilterChain whitelistBoilerplateSignup(ServerHttpSecurity http, Tracer tracer) {
    return http.securityMatcher(
            new PathPatternParserServerWebExchangeMatcher("/v1/auth/signup", HttpMethod.POST))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 4)
  public SecurityWebFilterChain whitelistVerifyAccount(ServerHttpSecurity http, Tracer tracer) {
    return http.securityMatcher(
            new RegexParserServerWebExchangeMatcher("/v1/auth/verification\\?token=.*",
                HttpMethod.GET))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE + 5)
  public SecurityWebFilterChain whitelistActuatorHealth(ServerHttpSecurity http, Tracer tracer) {
    return http.securityMatcher(
            new PathPatternParserServerWebExchangeMatcher("/actuator/health/**", HttpMethod.GET))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .build();
  }

  @SneakyThrows
  @Primary
  @Bean
  public SecurityWebFilterChain apiHttpSecurity(ServerHttpSecurity httpSecurity, // NOSONAR
      @Qualifier("BoilerplateSecurityAccessDeniedHandler") ServerAccessDeniedHandler accessDeniedHandler,
      @Qualifier("BoilerplateSecurityAuthenticationEntryPoint") ServerAuthenticationEntryPoint authenticationEntryPoint,
      @Qualifier("BoilerplateSecurityCorsFilter") CorsWebFilter corsWebFilter,
      CognitoIdTokenProcessor cognitoIdTokenProcessor,
      MicrosoftTokenProcessor microsoftTokenProcessor,
      GuestTokenProcessor guestTokenProcessor,
      Tracer tracer) {
    return httpSecurity.authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
        .cors().and()
        .requestCache().disable()
        .csrf().disable()
        .httpBasic().disable()
        .formLogin().disable()
        .exceptionHandling()
        .accessDeniedHandler(accessDeniedHandler)
        .authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .addFilterBefore(new LoggingFilter(tracer), SecurityWebFiltersOrder.FIRST)
        .addFilterAt(
            new BoilerplateSecurityAuthenticationFilter(cognitoIdTokenProcessor,
                microsoftTokenProcessor, guestTokenProcessor, authenticationEntryPoint),
            SecurityWebFiltersOrder.AUTHENTICATION)
        .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)
        .addFilterAfter(
            new BoilerplateSecurityAuthorizationFilter(
                accessDeniedHandler,
                authenticationEntryPoint
            ),
            SecurityWebFiltersOrder.AUTHENTICATION)
        .addFilterAfter(new ReactiveRequestContextFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
        .build();
  }
}
