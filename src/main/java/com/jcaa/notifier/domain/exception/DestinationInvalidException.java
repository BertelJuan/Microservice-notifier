package com.jcaa.notifier.domain.exception;

public final class DestinationInvalidException extends DomainException {

  private DestinationInvalidException(final String message) {
    super(message);
  }

  public static DestinationInvalidException becauseEmailIsEmpty() {
    return new DestinationInvalidException("El correo de destino no puede estar vacío.");
  }

  public static DestinationInvalidException becauseEmailFormatIsInvalid(final String email) {
    return new DestinationInvalidException("El correo de destino no es válido: " + email);
  }

  public static DestinationInvalidException becauseNameIsEmpty() {
    return new DestinationInvalidException("El nombre del destinatario no puede estar vacío.");
  }

  public static DestinationInvalidException becauseNameIsTooLong(final int maxLength) {
    return new DestinationInvalidException(
        "El nombre del destinatario no puede superar " + maxLength + " caracteres.");
  }
}