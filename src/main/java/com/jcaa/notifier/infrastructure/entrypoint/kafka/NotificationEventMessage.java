package com.jcaa.notifier.infrastructure.entrypoint.kafka;

import java.util.Map;

/** Forma del mensaje JSON que llega por Kafka. */
public record NotificationEventMessage(
    String eventId,
    String type,
    String occurredAt,
    Recipient recipient,
    Map<String, String> data) {

  public record Recipient(String email, String name) {}
}