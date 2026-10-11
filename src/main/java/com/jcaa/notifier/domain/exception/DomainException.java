package com.jcaa.notifier.domain.exception;

/** Base de todas las excepciones de negocio del dominio. */
public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }

  protected DomainException(final String message, final Throwable cause) {
    super(message, cause);
  }
}