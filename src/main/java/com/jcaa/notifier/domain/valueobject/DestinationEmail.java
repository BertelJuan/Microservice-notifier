package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.DestinationInvalidException;
import java.util.Locale;

public record DestinationEmail(String value) {

  public DestinationEmail {
    final String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) {
      throw DestinationInvalidException.becauseEmailIsEmpty();
    }
    if (!EmailFormat.isValid(normalized)) {
      throw DestinationInvalidException.becauseEmailFormatIsInvalid(normalized);
    }
    value = normalized;
  }
}