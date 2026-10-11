package com.jcaa.notifier.infrastructure.adapter.brevo;

import com.jcaa.notifier.application.port.out.SendNotificationPort;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import com.jcaa.notifier.domain.model.Notification;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Entrega correos por la API HTTP de Brevo (el SMTP está bloqueado en Render). */
@Component
public class BrevoEmailSenderAdapter implements SendNotificationPort {

  private static final String SEND_PATH = "/v3/smtp/email";

  private final RestClient restClient;
  private final String senderName;

  public BrevoEmailSenderAdapter(
      @Qualifier("brevoRestClient") final RestClient restClient,
      @Value("${notifier.sender.name}") final String senderName) {
    this.restClient = restClient;
    this.senderName = senderName;
  }

  @Override
  public void send(final Notification notification) {
    final Map<String, Object> body =
        Map.of(
            "sender", Map.of("name", senderName, "email", notification.sender().value()),
            "to",
                List.of(
                    Map.of(
                        "email", notification.destinationEmail().value(),
                        "name", notification.destinationName().value())),
            "subject", notification.subject().value(),
            "htmlContent", notification.content().value());

    try {
      restClient
          .post()
          .uri(SEND_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .toBodilessEntity();
    } catch (final RestClientException exception) {
      throw NotificationDeliveryException.becauseProviderFailed(
          notification.destinationEmail().value(), exception);
    }
  }
}