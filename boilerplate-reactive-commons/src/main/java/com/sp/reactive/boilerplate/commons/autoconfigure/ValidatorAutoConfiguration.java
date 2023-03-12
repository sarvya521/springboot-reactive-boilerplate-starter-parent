package com.sp.reactive.boilerplate.commons.autoconfigure;

import javax.validation.ConstraintValidatorFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

@Configuration
public class ValidatorAutoConfiguration {

  @Bean
  public ConstraintValidatorFactory constraintValidatorFactory(
      AutowireCapableBeanFactory beanFactory) {
    return new SpringConstraintValidatorFactory(beanFactory);
  }
}
