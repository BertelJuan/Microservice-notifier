package com.jcaa.notifier.application.port.out;

/** Puerto de salida: avisa al resto del sistema el resultado de una notificación. */
public interface PublishNotificationResultPort {

  void publishSent(String sourceEventId, String userId, String notificationType);

  void publishFailed(
      String sourceEventId, String userId, String notificationType, String reason);
}
