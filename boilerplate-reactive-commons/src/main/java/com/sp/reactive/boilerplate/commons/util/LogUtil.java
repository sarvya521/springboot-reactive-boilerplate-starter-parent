package com.sp.reactive.boilerplate.commons.util;

import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

@SuppressWarnings("squid:S1192")
public final class LogUtil {

  private LogUtil() {
    throw new AssertionError();
  }

  private enum Direction {
    IN, OUT
  }

  public static void fillMdcForIncomingApi(String target, String httpMethod,
      String xForwardedFor, HttpStatus requestStatus, String userAgent,
      String refer) {
    MDC.put("target", target);
    MDC.put("http_method", httpMethod);
    MDC.put("x-forwarded-for", xForwardedFor);
    MDC.put("referer", refer);
    MDC.put("user_agent", userAgent);
    MDC.put("http_status_code", String.valueOf(requestStatus.value()));
    MDC.put("direction", Direction.IN.name());
  }

  public static void updateStatusCode(HttpStatus httpStatus) {
    MDC.put("http_status_code", String.valueOf(httpStatus.value()));
  }

  public static void clearMdcForIncomingApi() {
    MDC.remove("target");
    MDC.remove("http_method");
    MDC.remove("http_status_code");
    MDC.remove("direction");
    MDC.remove("referer");
    MDC.remove("user_agent");
    MDC.remove("x-forwarded-for");
  }

  public static void fillMdcForOutgoingApi(String target, HttpMethod method,
      HttpStatus status) {
    MDC.put("target", target);
    MDC.put("http_method", method.name());
    MDC.put("http_status_code", String.valueOf(status.value()));
    MDC.put("direction", Direction.OUT.name());
  }

  public static void clearMdcForOutgoingApi() {
    MDC.remove("target");
    MDC.remove("http_method");
    MDC.remove("http_status_code");
    MDC.remove("direction");
  }

  public static void fillCommonMdc(ServerHttpRequest request) {
    if (request.getHeaders().containsKey("x-forwarded-for")) {
      MDC.put("user_ip", request.getHeaders().getFirst("x-forwarded-for").split(",")[0]);
    } else if (Objects.nonNull(request.getRemoteAddress())) {
      MDC.put("user_ip", request.getRemoteAddress().toString());
    }
    MDC.put("api", request.getMethod() + "-" + request.getURI());
  }
}

