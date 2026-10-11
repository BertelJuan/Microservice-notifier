package com.jcaa.notifier.infrastructure.entrypoint.kafka;

import com.jcaa.notifier.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notifier.application.service.dto.command.NotificationEventCommand;
import com.jcaa.notifier.domain.exception.DomainException;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Entry point: escucha los tópicos de Kafka y delega en el caso de uso. */
@Component
public class NotificationEventListener {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

  private final ProcessNotificationEventUseCase processNotificationEventUseCase;
  private final JsonMapper jsonMapper;

  public NotificationEventListener(
      final ProcessNotificationEventUseCase processNotificationEventUseCase,
      final JsonMapper jsonMapper) {
    this.processNotificationEventUseCase = processNotificationEventUseCase;
    this.jsonMapper = jsonMapper;
  }

  @KafkaListener(topics = "#{'${notifier.kafka.topics}'.split(',')}")
  public void onMessage(final String payload) {
    try {
      final NotificationEventMessage message =
          jsonMapper.readValue(payload, NotificationEventMessage.class);
      processNotificationEventUseCase.execute(toCommand(message));
    } catch (final NotificationDeliveryException deliveryFailure) {
      // Brevo falló: relanzamos para que Kafka reintente el mensaje.
      throw deliveryFailure;
    } catch (final JacksonException | DomainException invalidMessage) {
      // Mensaje mal formado o con datos inválidos: reintentar no lo arregla, se descarta.
      log.warn("Mensaje descartado: {}", invalidMessage.getMessage());
    }
  }

  private static NotificationEventCommand toCommand(final NotificationEventMessage message) {
    final NotificationEventMessage.Recipient recipient = message.recipient();
    return new NotificationEventCommand(
        message.eventId(),
        message.type(),
        recipient == null ? null : recipient.email(),
        recipient == null ? null : recipient.name(),
        message.data());
  }
}