package com.jcaa.notifier.infrastructure.adapter.kafka;

import com.jcaa.notifier.application.port.out.PublishNotificationResultPort;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class KafkaNotificationResultPublisher implements PublishNotificationResultPort {

  private static final Logger log = LoggerFactory.getLogger(KafkaNotificationResultPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final JsonMapper jsonMapper;
  private final String topic;

  public KafkaNotificationResultPublisher(
      final KafkaTemplate<String, String> kafkaTemplate,
      final JsonMapper jsonMapper,
      @Value("${notifier.kafka.result-topic:notifications.events}") final String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.jsonMapper = jsonMapper;
    this.topic = topic;
  }

  @Override
  public void publishSent(
      final String sourceEventId, final String userId, final String notificationType) {
    publish("NOTIFICATION_SENT", sourceEventId, userId, notificationType, null);
  }

  @Override
  public void publishFailed(
      final String sourceEventId,
      final String userId,
      final String notificationType,
      final String reason) {
    publish("NOTIFICATION_FAILED", sourceEventId, userId, notificationType, reason);
  }

  private void publish(
      final String type,
      final String sourceEventId,
      final String userId,
      final String notificationType,
      final String reason) {
    try {
      final Map<String, Object> data = new LinkedHashMap<>();
      data.put("userId", userId);
      data.put("sourceEventId", sourceEventId);
      data.put("notificationType", notificationType);
      if (reason != null) {
        data.put("reason", reason);
      }
      final Map<String, Object> event = new LinkedHashMap<>();
      event.put("eventId", UUID.randomUUID().toString());
      event.put("type", type);
      event.put("occurredAt", Instant.now().toString());
      event.put("data", data);

      kafkaTemplate.send(topic, userId, jsonMapper.writeValueAsString(event));
    } catch (final RuntimeException exception) {
      // Nunca debe tumbar el flujo: si falla la confirmación solo se registra.
      log.warn(
          "No se pudo publicar {} de {}: {}", type, sourceEventId, exception.getMessage());
    }
  }
}
