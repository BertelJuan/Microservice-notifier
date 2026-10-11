package com.jcaa.notifier.domain.exception;

public final class ContentInvalidException extends DomainException {

  private ContentInvalidException(final String message) {
    super(message);
  }

  public static ContentInvalidException becauseValueIsEmpty() {
    return new ContentInvalidException("El contenido del mensaje no puede estar vacío.");
  }
}