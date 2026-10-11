package com.jcaa.notifier.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.notifier.domain.enums.NotificationType;
import com.jcaa.notifier.domain.exception.ContentInvalidException;
import com.jcaa.notifier.domain.exception.DestinationInvalidException;
import com.jcaa.notifier.domain.exception.NotificationIdInvalidException;
import com.jcaa.notifier.domain.exception.NotificationTypeInvalidException;
import com.jcaa.notifier.domain.exception.SenderInvalidException;
import com.jcaa.notifier.domain.exception.SubjectInvalidException;
import org.junit.jupiter.api.Test;

class NotificationTest {

  private static Notification build(
      final String type,
      final String sender,
      final String to,
      final String subject,
      final String body) {
    return Notification.create("evt-1", type, sender, to, "Ana", subject, body);
  }

  @Test
  void createsValidNotificationAndNormalizesValues() {
    final Notification notification =
        build("user_created", " No-Reply@Mail.com ", " ANA@Correo.COM ", " Hola ", "<p>x</p>");

    assertEquals(NotificationType.USER_CREATED, notification.type());
    assertEquals("no-reply@mail.com", notification.sender().value());
    assertEquals("ana@correo.com", notification.destinationEmail().value());
    assertEquals("Hola", notification.subject().value());
  }

  @Test
  void rejectsInvalidDestinationEmail() {
    assertThrows(
        DestinationInvalidException.class,
        () -> build("USER_CREATED", "a@b.com", "no-es-correo", "s", "c"));
    assertThrows(
        DestinationInvalidException.class, () -> build("USER_CREATED", "a@b.com", " ", "s", "c"));
    assertThrows(
        DestinationInvalidException.class, () -> build("USER_CREATED", "a@b.com", null, "s", "c"));
  }

  @Test
  void rejectsInvalidSender() {
    assertThrows(
        SenderInvalidException.class, () -> build("USER_CREATED", "x", "ana@correo.com", "s", "c"));
  }

  @Test
  void rejectsEmptySubjectAndContent() {
    assertThrows(
        SubjectInvalidException.class,
        () -> build("USER_CREATED", "a@b.com", "ana@correo.com", " ", "c"));
    assertThrows(
        ContentInvalidException.class,
        () -> build("USER_CREATED", "a@b.com", "ana@correo.com", "s", " "));
  }

  @Test
  void rejectsUnknownTypeAndEmptyId() {
    assertThrows(
        NotificationTypeInvalidException.class,
        () -> build("OTRO", "a@b.com", "ana@correo.com", "s", "c"));
    assertThrows(
        NotificationIdInvalidException.class,
        () ->
            Notification.create(
                " ", "USER_CREATED", "a@b.com", "ana@correo.com", "Ana", "s", "c"));
  }
}