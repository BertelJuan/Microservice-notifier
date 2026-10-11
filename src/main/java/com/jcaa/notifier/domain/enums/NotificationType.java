package com.jcaa.notifier.domain.enums;

import com.jcaa.notifier.domain.exception.NotificationTypeInvalidException;
import java.util.Arrays;
import java.util.Locale;

public enum NotificationType {
  USER_CREATED,
  USER_UPDATED,
  ACCOUNT_CREATED;

  public static NotificationType fromString(final String value) {
    if (value == null || value.isBlank()) {
      throw NotificationTypeInvalidException.becauseValueIsEmpty();
    }
    final String normalized = value.trim().toUpperCase(Locale.ROOT);
    return Arrays.stream(values())
        .filter(type -> type.name().equals(normalized))
        .findFirst()
        .orElseThrow(() -> NotificationTypeInvalidException.becauseValueIsUnknown(value));
  }
}