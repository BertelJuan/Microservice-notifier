package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.SenderInvalidException;
import java.util.Locale;

public record SenderEmail(String value) {

  public SenderEmail {
    final String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw SenderInvalidException.becauseEmailIsEmpty();
    }
    if (!EmailFormat.isValid(normalized)) {
      throw SenderInvalidException.becauseEmailFormatIsInvalid(normalized);
    }
    value = normalized;
  }
}