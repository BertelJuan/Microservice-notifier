package com.jcaa.notifier.domain.valueobject;

import com.jcaa.notifier.domain.exception.ContentInvalidException;

public record NotificationContent(String value) {

  public NotificationContent {
    if (value == null || value.isBlank()) {
      throw ContentInvalidException.becauseValueIsEmpty();
    }
  }
}