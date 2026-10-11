package com.jcaa.notifier.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.notifier.application.service.dto.command.NotificationEventCommand;
import com.jcaa.notifier.domain.enums.NotificationType;
import com.jcaa.notifier.domain.exception.DestinationInvalidException;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import com.jcaa.notifier.domain.exception.NotificationTypeInvalidException;
import com.jcaa.notifier.domain.model.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationEventServiceTest {

  private static final String SENDER = "no-reply@mi-dominio.com";

  private final List<Notification> sent = new ArrayList<>();

  // El puerto de salida es una interfaz con un solo método: aquí lo reemplazamos por un "falso".
  private final NotificationEventService service =
      new NotificationEventService(sent::add, new EmailMessageComposer(), SENDER);

  private static NotificationEventCommand event(final String type, final String email) {
    return new NotificationEventCommand(
        "evt-1", type, email, "Ana", Map.of("role", "MEMBER", "accountId", "123"));
  }

  @Test
  void buildsAndSendsANotificationFromTheEvent() {
    service.execute(event("USER_CREATED", "Ana@Correo.com"));

    assertEquals(1, sent.size());
    final Notification notification = sent.get(0);
    assertEquals(NotificationType.USER_CREATED, notification.type());
    assertEquals(SENDER, notification.sender().value());
    assertEquals("ana@correo.com", notification.destinationEmail().value());
    assertTrue(notification.subject().value().contains("creada"));
  }

  @Test
  void doesNotSendWhenTheDestinationIsInvalid() {
    assertThrows(
        DestinationInvalidException.class, () -> service.execute(event("USER_CREATED", "malo")));
    assertTrue(sent.isEmpty());
  }

  @Test
  void doesNotSendWhenTheTypeIsUnknown() {
    assertThrows(
        NotificationTypeInvalidException.class,
        () -> service.execute(event("OTRO", "ana@correo.com")));
    assertTrue(sent.isEmpty());
  }

  @Test
  void propagatesDeliveryFailures() {
    final NotificationEventService failing =
        new NotificationEventService(
            notification -> {
              throw NotificationDeliveryException.becauseProviderFailed(
                  notification.destinationEmail().value(), new RuntimeException("Brevo caído"));
            },
            new EmailMessageComposer(),
            SENDER);

    assertThrows(
        NotificationDeliveryException.class,
        () -> failing.execute(event("ACCOUNT_CREATED", "ana@correo.com")));
  }
}