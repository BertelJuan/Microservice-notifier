package com.jcaa.notifier.domain.exception;

public final class SubjectInvalidException extends DomainException {

  private SubjectInvalidException(final String message) {
    super(message);
  }

  public static SubjectInvalidException becauseValueIsEmpty() {
    return new SubjectInvalidException("El asunto no puede estar vacío.");
  }

  public static SubjectInvalidException becauseValueIsTooLong(final int maxLength) {
    return new SubjectInvalidException("El asunto no puede superar " + maxLength + " caracteres.");
  }
}