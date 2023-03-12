package com.sp.reactive.boilerplate.commons.exception;

import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import lombok.Getter;

public class BoilerplateException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * see {@link ErrorDetails}
   */
  @Getter
  private final ErrorDetails error;


  /**
   * @param error ErrorDetails
   * @see RuntimeException#RuntimeException(String)
   */
  public BoilerplateException(ErrorDetails error) {
    super(error.toString());
    this.error = error;
  }

  /**
   * @param error ErrorDetails
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *              unknown.)
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public BoilerplateException(ErrorDetails error, Throwable cause) {
    super(error.toString(), cause);
    this.error = error;
  }
}
