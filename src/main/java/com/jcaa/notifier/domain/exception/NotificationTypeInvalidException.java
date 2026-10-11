package com.jcaa.notifier.domain.exception;

public final class NotificationTypeInvalidException extends DomainException {

  private NotificationTypeInvalidException(final String message) {
    super(message);
  }

  public static NotificationTypeInvalidException becauseValueIsEmpty() {
    return new NotificationTypeInvalidException("El tipo de notificación no puede estar vacío.");
  }

  public static NotificationTypeInvalidException becauseValueIsUnknown(final String value) {
    return new NotificationTypeInvalidException("Tipo de notificación desconocido: " + value);
  }
}