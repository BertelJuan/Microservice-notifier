package com.jcaa.notifier.application.service.dto.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Datos del evento recibido (por ejemplo desde Kafka) que debe convertirse en un correo. */
public record NotificationEventCommand(
    String eventId,
    String type,
    String recipientEmail,
    String recipientName,
    Map<String, String> data) {

  public NotificationEventCommand {
    data = data == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(data));
  }
}