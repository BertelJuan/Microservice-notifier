package com.jcaa.notifier.domain.exception;

public final class SenderInvalidException extends DomainException {

  private SenderInvalidException(final String message) {
    super(message);
  }

  public static SenderInvalidException becauseEmailIsEmpty() {
    return new SenderInvalidException("El correo del remitente no puede estar vacío.");
  }

  public static SenderInvalidException becauseEmailFormatIsInvalid(final String email) {
    return new SenderInvalidException("El correo del remitente no es válido: " + email);
  }
}