package com.sp.reactive.boilerplate.commons.autoconfigure;

import com.sp.reactive.boilerplate.commons.util.ErrorGenerator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class ErrorMessageSourceAutoConfiguration {

  @Bean
  public MessageSource errorMessageSource() {
    ReloadableResourceBundleMessageSource messageSource
        = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:errors-list");
    messageSource.setDefaultEncoding("UTF-8");

    ErrorGenerator.setErrorMessageSource(messageSource);

    return messageSource;
  }
}
