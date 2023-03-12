package com.sp.reactive.boilerplate.security;

import static ch.qos.logback.classic.Level.DEBUG;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.sp.reactive.boilerplate.commons.util.LogUtil;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class LoggingFilter implements WebFilter {

  private static final String TRACKING_HEADER = "X-Trace-Id";
  private static final String SAMPLING_HEADER = "debug-enabled";
  private static final String BASE_PACKAGE = "com.sp";
  private static final Set<String> BOOLEAN_VALUES = Set.of("true", "yes", "y", "1");
  private Tracer tracer;

  public LoggingFilter(Tracer tracer) {
    this.tracer = tracer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    boolean enableDebug = request.getHeaders().containsKey(SAMPLING_HEADER);
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Level existingRootLevel = context.getLogger(ROOT_LOGGER_NAME).getLevel();
    Level existingAppLevel = context.getLogger(BASE_PACKAGE).getLevel();
    if (enableDebug) {
      context.getLogger(ROOT_LOGGER_NAME).setLevel(DEBUG);
      context.getLogger(BASE_PACKAGE).setLevel(DEBUG);
    }

    ServerHttpResponse response = exchange.getResponse();
    attacheTraceId(response);
    LogUtil.fillCommonMdc(request);

    Mono<Void> filter = chain.filter(exchange);

    MDC.clear();

    if (enableDebug) {
      context.getLogger(ROOT_LOGGER_NAME).setLevel(existingRootLevel);
      context.getLogger(BASE_PACKAGE).setLevel(existingAppLevel);
    }

    return filter;
  }

  /**
   * Attach trace Id to the Response
   *
   * @param response {@link ServerHttpResponse}
   */
  private void attacheTraceId(ServerHttpResponse response) {
    final Span currentSpan = tracer.currentSpan();
    if (null != currentSpan) {
      final String traceId = currentSpan.context().traceId();
      log.debug("added tracking id in response - {}", traceId);
      response.getHeaders().set(TRACKING_HEADER, traceId);
    }
  }
}
