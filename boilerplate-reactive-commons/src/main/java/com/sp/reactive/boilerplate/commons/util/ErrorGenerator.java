package com.sp.reactive.boilerplate.commons.util;

import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import java.util.Objects;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;


@SuppressWarnings("squid:S2637")
public final class ErrorGenerator {

  private static final String DEFAULT_MESSAGE = "Unknown error. Please contact support";

  @SuppressWarnings("squid:S3008")
  private static MessageSource ERROR_MESSAGE_SOURCE;

  private ErrorGenerator() {
    throw new AssertionError();
  }

  public static void setErrorMessageSource(MessageSource errorMessageSource) {
    ERROR_MESSAGE_SOURCE = errorMessageSource;
  }

  public static ErrorDetails generateForCode(String code) {
    return
        new ErrorDetails(
            code,
            Objects.requireNonNull(ERROR_MESSAGE_SOURCE
                .getMessage(
                    code,
                    null,
                    DEFAULT_MESSAGE,
                    LocaleContextHolder.getLocale()
                ))
        );
  }

  public static ErrorDetails generateForCodeWithTarget(String code, String target) {
    return
        new ErrorDetails(
            code,
            Objects.requireNonNull(ERROR_MESSAGE_SOURCE
                .getMessage(
                    code,
                    null,
                    DEFAULT_MESSAGE,
                    LocaleContextHolder.getLocale()
                )),
            target
        );
  }

  public static ErrorDetails generateForCodeWithArguments(String code, Object... args) {
    return
        new ErrorDetails(
            code,
            Objects.requireNonNull(ERROR_MESSAGE_SOURCE
                .getMessage(
                    code,
                    args,
                    DEFAULT_MESSAGE,
                    LocaleContextHolder.getLocale()
                ))
        );
  }

  public static ErrorDetails generateForCodeWithArgumentsAndTarget(String code, String target,
      Object... args) {
    return
        new ErrorDetails(
            code,
            Objects.requireNonNull(ERROR_MESSAGE_SOURCE.getMessage(
                code,
                args,
                DEFAULT_MESSAGE,
                LocaleContextHolder.getLocale()
            )),
            target
        );
  }
}
