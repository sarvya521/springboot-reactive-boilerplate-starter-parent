package com.sp.reactive.boilerplate.security;

import com.sp.reactive.boilerplate.commons.dto.ErrorDetails;
import com.sp.reactive.boilerplate.commons.exception.BoilerplateException;

public class TokenException extends BoilerplateException {

  /**
   * @param errorDetails ErrorDetails
   * @see BoilerplateException#BoilerplateException(ErrorDetails)
   */
  public TokenException(ErrorDetails errorDetails) {
    super(errorDetails);
  }

  /**
   * @param errorDetails ErrorDetails
   * @param cause        the cause (which is saved for later retrieval by the {@link #getCause()}
   *                     method). (A {@code null} value is permitted, and indicates that the cause
   *                     is nonexistent or unknown.)
   * @see BoilerplateException#BoilerplateException(ErrorDetails, Throwable)
   */
  public TokenException(ErrorDetails errorDetails, Throwable cause) {
    super(errorDetails, cause);
  }
}
