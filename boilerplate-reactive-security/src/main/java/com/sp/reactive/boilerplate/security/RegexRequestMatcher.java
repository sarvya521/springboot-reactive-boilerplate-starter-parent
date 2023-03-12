package com.sp.reactive.boilerplate.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class RegexRequestMatcher {

  private static final Cache<String, Pattern> CACHE = CacheBuilder.newBuilder().build();
  private static final ConcurrentMap<String, Pattern> PATTERNS = CACHE.asMap();

  private static Mono<ServerWebExchangeMatcher.MatchResult> matches(String regex, String uri) {
    Pattern pattern = getPattern(regex);
    Matcher matcher = pattern.matcher(uri);
    boolean matches = matcher.matches();
    return Mono.just(matches)
        .flatMap(m -> m ? ServerWebExchangeMatcher.MatchResult.match()
            : ServerWebExchangeMatcher.MatchResult.notMatch());
  }

  private static Pattern getPattern(String regex) {
    return PATTERNS.computeIfAbsent(regex, Pattern::compile);
  }

  public static Mono<ServerWebExchangeMatcher.MatchResult> matches(HttpMethod method, String regex,
      ServerWebExchange exchange) {
    if (!Objects.equals(method, exchange.getRequest().getMethod())) {
      return ServerWebExchangeMatcher.MatchResult.notMatch();
    }
    return matches(regex, exchange.getRequest().getURI().getPath());
  }
}
