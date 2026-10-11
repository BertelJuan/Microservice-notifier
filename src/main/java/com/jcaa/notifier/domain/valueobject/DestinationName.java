package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.DestinationInvalidException;

public record DestinationName(String value) {

  private static final int MAX_LENGTH = 100;

  public DestinationName {
    final String normalized = value == null ? "" : value.trim();
    if (normalized.isEmpty()) {
      throw DestinationInvalidException.becauseNameIsEmpty();
    }
    if (normalized.length() > MAX_LENGTH) {
      throw DestinationInvalidException.becauseNameIsTooLong(MAX_LENGTH);
    }
    value = normalized;
  }
}