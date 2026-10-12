package com.jcaa.notifier.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

  @Bean
  public DefaultErrorHandler kafkaErrorHandler(final NotificationFailureRecoverer recoverer) {
    // 1 intento + 4 reintentos = 5 intentos, 2 s entre cada uno. Al agotarse: NOTIFICATION_FAILED.
    return new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 4L));
  }
}
