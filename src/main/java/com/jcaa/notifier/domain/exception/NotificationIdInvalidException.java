package com.jcaa.notifier.domain.exception;

public final class NotificationIdInvalidException extends DomainException {

  private NotificationIdInvalidException(final String message) {
    super(message);
  }

  public static NotificationIdInvalidException becauseValueIsEmpty() {
    return new NotificationIdInvalidException("El id de la notificación no puede estar vacío.");
  }

  public static NotificationIdInvalidException becauseValueIsTooLong(final int maxLength) {
    return new NotificationIdInvalidException(
        "El id de la notificación no puede superar " + maxLength + " caracteres.");
  }
}