package com.sp.reactive.boilerplate.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Wrapper of error details that has to be sent in Api Response
 *
 * @see Response
 */
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@JsonPropertyOrder({"code", "message", "target"})
@Getter
@Setter
@NoArgsConstructor
public class ErrorDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Service Error Code
   */
  private String code;

  /**
   * Service Error Message
   */
  private String message;

  /**
   * Error's whereabouts
   */
  private String target;

  /**
   * @param code    Error Code
   * @param message Error Message
   */
  public ErrorDetails(@NonNull String code, @NonNull String message) {
    this.code = code;
    this.message = message;
  }

  /**
   * @param code    Error Code
   * @param message Error Message
   * @param target  Error's whereabouts
   */
  public ErrorDetails(@NonNull String code, @NonNull String message, @NonNull String target) {
    this.code = code;
    this.message = message;
    this.target = target;
  }

  @Override
  public String toString() {
    String error = String.format("%s-%s", code, message);
    if (null != target && !target.isBlank()) {
      error = String.format("%s target:%s", error, target);
    }
    return error;
  }
}
