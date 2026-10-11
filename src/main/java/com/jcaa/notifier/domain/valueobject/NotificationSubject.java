package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.SubjectInvalidException;

public record NotificationSubject(String value) {

  private static final int MAX_LENGTH = 200;

  public NotificationSubject {
    final String normalized = value == null ? "" : value.trim();
    if (normalized.isEmpty()) {
      throw SubjectInvalidException.becauseValueIsEmpty();
    }
    if (normalized.length() > MAX_LENGTH) {
      throw SubjectInvalidException.becauseValueIsTooLong(MAX_LENGTH);
    }
    value = normalized;
  }
}