package com.jcaa.notifier.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class BrevoClientConfig {

  @Bean
  public RestClient brevoRestClient(
      @Value("${brevo.api-url}") final String apiUrl,
      @Value("${brevo.api-key}") final String apiKey) {
    final HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofSeconds(10));

    return RestClient.builder()
        .baseUrl(apiUrl)
        .requestFactory(requestFactory)
        .defaultHeader("api-key", apiKey)
        .build();
  }
}