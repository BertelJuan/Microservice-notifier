package com.jcaa.notifier.domain.valueobject;

import java.util.regex.Pattern;

/** Validación de formato de correo compartida por los value objects de correo. */
final class EmailFormat {

  private static final int MAX_LENGTH = 254;
  private static final Pattern PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

  private EmailFormat() {}

  static boolean isValid(final String email) {
    return email.length() <= MAX_LENGTH && PATTERN.matcher(email).matches();
  }
}