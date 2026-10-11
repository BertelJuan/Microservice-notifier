package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.NotificationIdInvalidException;

public record NotificationId(String value) {

  private static final int MAX_LENGTH = 64;

  public NotificationId {
    final String normalized = value == null ? "" : value.trim();
    if (normalized.isEmpty()) {
      throw NotificationIdInvalidException.becauseValueIsEmpty();
    }
    if (normalized.length() > MAX_LENGTH) {
      throw NotificationIdInvalidException.becauseValueIsTooLong(MAX_LENGTH);
    }
    value = normalized;
  }
}