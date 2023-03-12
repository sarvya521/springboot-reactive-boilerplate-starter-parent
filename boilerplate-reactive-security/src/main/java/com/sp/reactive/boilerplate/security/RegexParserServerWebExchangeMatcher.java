package com.sp.reactive.boilerplate.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class RegexParserServerWebExchangeMatcher implements ServerWebExchangeMatcher {

  private static final Log logger = LogFactory.getLog(RegexParserServerWebExchangeMatcher.class);
  private final Pattern pattern;
  private final HttpMethod method;

  public RegexParserServerWebExchangeMatcher(String regex, HttpMethod method) {
    Assert.notNull(regex, "pattern cannot be null");
    this.pattern = Pattern.compile(regex);
    this.method = method;
  }

  public Mono<MatchResult> matches(ServerWebExchange exchange) {
    ServerHttpRequest request = exchange.getRequest();
    PathContainer path = request.getPath().pathWithinApplication();
    Matcher matcher = pattern.matcher(request.getURI().toString());

    if (this.method != null && !this.method.equals(request.getMethod())) {
      return MatchResult.notMatch().doOnNext((result) -> {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Request '" + request.getMethod() + " " + path + "' doesn't match '" + this.method
                  + " " + this.pattern.pattern() + "'");
        }
      });
    } else {
      boolean match = matcher.matches();
      if (!match) {
        return MatchResult.notMatch().doOnNext((result) -> {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "Request '" + request.getMethod() + " " + path + "' doesn't match '" + this.method
                    + " " + this.pattern.pattern() + "'");
          }
        });
      } else {
        return MatchResult.match();
      }
    }
  }

  public String toString() {
    return "RegexParserServerWebExchangeMatcher{pattern='" + this.pattern + '\'' + ", method="
        + this.method + '}';
  }
}
