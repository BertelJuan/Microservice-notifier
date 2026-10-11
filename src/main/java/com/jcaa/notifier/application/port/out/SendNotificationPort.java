package com.jcaa.notifier.application.port.out;

import com.jcaa.notifier.domain.model.Notification;

/**
 * Puerto de salida: entrega una notificación por un proveedor externo (Brevo).
 * Lanza NotificationDeliveryException si el proveedor falla.
 */
public interface SendNotificationPort {

  void send(Notification notification);
}