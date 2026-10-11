package com.jcaa.notifier.infrastructure.entrypoint.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.notifier.application.service.dto.command.NotificationEventCommand;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import com.jcaa.notifier.domain.valueobject.DestinationEmail;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class NotificationEventListenerTest {

  private static final String VALID_JSON =
      """
      {"eventId":"evt-1","type":"USER_CREATED",
       "recipient":{"email":"ana@correo.com","name":"Ana"},
       "data":{"role":"MEMBER"}}
      """;

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private final List<NotificationEventCommand> received = new ArrayList<>();

  @Test
  void convertsTheJsonIntoACommandAndCallsTheUseCase() {
    new NotificationEventListener(received::add, jsonMapper).onMessage(VALID_JSON);

    assertEquals(1, received.size());
    assertEquals("evt-1", received.get(0).eventId());
    assertEquals("ana@correo.com", received.get(0).recipientEmail());
    assertEquals("MEMBER", received.get(0).data().get("role"));
  }

  @Test
  void discardsMessagesThatAreNotJson() {
    new NotificationEventListener(received::add, jsonMapper).onMessage("esto no es json");

    assertTrue(received.isEmpty());
  }

  @Test
  void discardsMessagesWithInvalidData() {
    // El caso de uso lanza una excepción de negocio: el mensaje se descarta sin reventar.
    final NotificationEventListener listener =
        new NotificationEventListener(command -> new DestinationEmail("malo"), jsonMapper);

    listener.onMessage(VALID_JSON);
  }

  @Test
  void rethrowsDeliveryFailuresSoKafkaCanRetry() {
    final NotificationEventListener listener =
        new NotificationEventListener(
            command -> {
              throw NotificationDeliveryException.becauseProviderFailed(
                  "ana@correo.com", new RuntimeException("Brevo caído"));
            },
            jsonMapper);

    assertThrows(NotificationDeliveryException.class, () -> listener.onMessage(VALID_JSON));
  }
}