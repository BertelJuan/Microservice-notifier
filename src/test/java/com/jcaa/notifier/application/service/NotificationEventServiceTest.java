package com.jcaa.notifier.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.notifier.application.port.out.PublishNotificationResultPort;
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

  /** Falso del puerto de confirmación: guarda lo que se publicaría. */
  private static final class RecordingResultPort implements PublishNotificationResultPort {
    final List<String> sent = new ArrayList<>();
    final List<String> failed = new ArrayList<>();

    @Override
    public void publishSent(final String sourceEventId, final String userId, final String type) {
      sent.add(sourceEventId + "|" + userId + "|" + type);
    }

    @Override
    public void publishFailed(
        final String sourceEventId, final String userId, final String type, final String reason) {
      failed.add(sourceEventId + "|" + userId + "|" + type + "|" + reason);
    }
  }

  private final List<Notification> sent = new ArrayList<>();
  private final RecordingResultPort results = new RecordingResultPort();

  private final NotificationEventService service =
      new NotificationEventService(sent::add, new EmailMessageComposer(), results, SENDER);

  private static NotificationEventCommand event(final String type, final String email) {
    return new NotificationEventCommand(
        "evt-1",
        type,
        email,
        "Ana",
        Map.of("userId", "usr-1", "role", "MEMBER", "accountNumber", "7159968480"));
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
  void publishesConfirmationAfterSending() {
    service.execute(event("USER_CREATED", "ana@correo.com"));

    assertEquals(List.of("evt-1|usr-1|USER_CREATED"), results.sent);
  }

  @Test
  void doesNotSendWhenTheDestinationIsInvalid() {
    assertThrows(
        DestinationInvalidException.class, () -> service.execute(event("USER_CREATED", "malo")));
    assertTrue(sent.isEmpty());
    assertTrue(results.sent.isEmpty());
  }

  @Test
  void doesNotSendWhenTheTypeIsUnknown() {
    assertThrows(
        NotificationTypeInvalidException.class,
        () -> service.execute(event("OTRO", "ana@correo.com")));
    assertTrue(sent.isEmpty());
  }

  @Test
  void propagatesDeliveryFailuresAndDoesNotConfirm() {
    final RecordingResultPort failingResults = new RecordingResultPort();
    final NotificationEventService failing =
        new NotificationEventService(
            notification -> {
              throw NotificationDeliveryException.becauseProviderFailed(
                  notification.destinationEmail().value(), new RuntimeException("Brevo caído"));
            },
            new EmailMessageComposer(),
            failingResults,
            SENDER);

    assertThrows(
        NotificationDeliveryException.class,
        () -> failing.execute(event("ACCOUNT_CREATED", "ana@correo.com")));
    assertTrue(failingResults.sent.isEmpty());
  }
}
