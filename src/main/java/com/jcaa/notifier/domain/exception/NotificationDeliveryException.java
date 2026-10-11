package com.jcaa.notifier.domain.exception;

/** El proveedor externo de correo no pudo entregar la notificación. */
public final class NotificationDeliveryException extends DomainException {

  private NotificationDeliveryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static NotificationDeliveryException becauseProviderFailed(
      final String destinationEmail, final Throwable cause) {
    return new NotificationDeliveryException(
        "No se pudo enviar la notificación a " + destinationEmail, cause);
  }
}