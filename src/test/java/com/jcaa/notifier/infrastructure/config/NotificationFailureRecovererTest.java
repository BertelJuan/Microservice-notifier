package com.jcaa.notifier.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.notifier.application.port.out.PublishNotificationResultPort;
import com.jcaa.notifier.domain.exception.NotificationDeliveryException;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import tools.jackson.databind.json.JsonMapper;

class NotificationFailureRecovererTest {

  private static final String USER_JSON =
      """
      {"eventId":"evt-1","type":"USER_CREATED",
       "recipient":{"email":"ana@correo.com","name":"Ana"},
       "data":{"userId":"usr-1","role":"ADMIN"}}
      """;

  private final List<String> failed = new ArrayList<>();

  private final PublishNotificationResultPort port =
      new PublishNotificationResultPort() {
        @Override
        public void publishSent(final String s, final String u, final String t) {}

        @Override
        public void publishFailed(final String s, final String u, final String t, final String r) {
          failed.add(s + "|" + u + "|" + t + "|" + r);
        }
      };

  private final NotificationFailureRecoverer recoverer =
      new NotificationFailureRecoverer(port, JsonMapper.builder().build());

  private static ConsumerRecord<String, String> record(final String value) {
    return new ConsumerRecord<>("users.events", 0, 0L, "usr-1", value);
  }

  private static Exception brevoFailure() {
    return new ListenerExecutionFailedException(
        "falló",
        NotificationDeliveryException.becauseProviderFailed(
            "ana@correo.com", new RuntimeException("sin red")));
  }

  @Test
  void publishesFailedWhenBrevoKeptFailing() {
    recoverer.accept(record(USER_JSON), brevoFailure());

    assertEquals(List.of("evt-1|usr-1|USER_CREATED|BREVO_UNAVAILABLE"), failed);
  }

  @Test
  void ignoresFailuresThatAreNotFromBrevo() {
    recoverer.accept(record(USER_JSON), new IllegalStateException("otro error"));

    assertTrue(failed.isEmpty());
  }

  @Test
  void neverThrowsEvenIfThePayloadIsBroken() {
    recoverer.accept(record("no es json"), brevoFailure());

    assertTrue(failed.isEmpty());
  }
}
