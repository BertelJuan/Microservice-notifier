package com.jcaa.notifier.infrastructure.config;

import com.jcaa.notifier.application.port.out.PublishNotificationResultPort;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import com.jcaa.notifier.domain.enums.NotificationType;
import com.jcaa.notifier.infrastructure.entrypoint.kafka.NotificationEventMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Se ejecuta cuando Kafka agotó los reintentos de un mensaje. Si la causa fue Brevo, publica
 * NOTIFICATION_FAILED para que users no deje al usuario colgado en "notificando".
 */
@Component
public class NotificationFailureRecoverer implements ConsumerRecordRecoverer {

  static final String BREVO_UNAVAILABLE = "BREVO_UNAVAILABLE";
  static final String BREVO_REJECTED = "BREVO_REJECTED";

  private static final Logger log = LoggerFactory.getLogger(NotificationFailureRecoverer.class);

  private final PublishNotificationResultPort resultPort;
  private final JsonMapper jsonMapper;

  public NotificationFailureRecoverer(
      final PublishNotificationResultPort resultPort, final JsonMapper jsonMapper) {
    this.resultPort = resultPort;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public void accept(final ConsumerRecord<?, ?> record, final Exception exception) {
    try {
      handle(record, exception);
    } catch (final RuntimeException unexpected) {
      // Si el recoverer lanzara, Kafka volvería a reintentar en bucle: aquí solo se registra.
      log.error("No se pudo procesar el fallo del mensaje: {}", unexpected.getMessage());
    }
  }

  private void handle(final ConsumerRecord<?, ?> record, final Exception exception) {
    log.error(
        "Reintentos agotados en {}-{}@{}: {}",
        record.topic(),
        record.partition(),
        record.offset(),
        exception.getMessage());

    if (!causedByDelivery(exception) || !(record.value() instanceof String payload)) {
      return; // No es un fallo de Brevo: se descarta con log.
    }
    final NotificationEventMessage message =
        jsonMapper.readValue(payload, NotificationEventMessage.class);
    final String userId = message.data() == null ? null : message.data().get("userId");
    if (userId == null || userId.isBlank()) {
      log.warn("Fallo de Brevo en {} sin userId: no hay a quién avisar.", message.eventId());
      return;
    }
    final String type = NotificationType.fromString(message.type()).name();
    resultPort.publishFailed(message.eventId(), userId, type, reasonOf(exception));
  }

  private static boolean causedByDelivery(final Throwable error) {
    return find(error, NotificationDeliveryException.class) != null;
  }

  static String reasonOf(final Throwable error) {
    return find(error, HttpClientErrorException.class) != null
        ? BREVO_REJECTED // 4xx: api key mala, remitente no verificado, etc.
        : BREVO_UNAVAILABLE; // red caída, timeout, 5xx
  }

  private static <T extends Throwable> T find(final Throwable error, final Class<T> type) {
    Throwable current = error;
    while (current != null) {
      if (type.isInstance(current)) {
        return type.cast(current);
      }
      current = current.getCause() == current ? null : current.getCause();
    }
    return null;
  }
}
