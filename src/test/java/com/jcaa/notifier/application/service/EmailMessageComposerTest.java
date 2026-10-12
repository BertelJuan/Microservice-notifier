package com.jcaa.notifier.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.notifier.domain.enums.NotificationType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmailMessageComposerTest {

  private final EmailMessageComposer composer = new EmailMessageComposer();

  @Test
  void userCreatedMentionsNameAndRoleButNeverAPassword() {
    final EmailMessage message =
        composer.compose(NotificationType.USER_CREATED, "Ana", Map.of("role", "MEMBER"));

    assertTrue(message.subject().contains("creada"));
    assertTrue(message.body().contains("Hola, Ana"));
    assertTrue(message.body().contains("MEMBER"));
    assertFalse(message.body().toLowerCase().contains("contraseña"));
  }

  @Test
  void userUpdatedOmitsFieldsThatAreMissing() {
    final EmailMessage message =
        composer.compose(NotificationType.USER_UPDATED, "Ana", Map.of("role", "ADMIN"));

    assertTrue(message.body().contains("ADMIN"));
    assertFalse(message.body().contains("Estado"));
  }

  @Test
  void accountCreatedShowsMaskedAccountNumber() {
    final EmailMessage message =
        composer.compose(
            NotificationType.ACCOUNT_CREATED,
            "Ana",
            Map.of("accountId", "uuid-interno", "accountNumber", "7159968480"));

    assertTrue(message.subject().contains("lista"));
    assertTrue(message.body().contains("******8480"));
    assertFalse(message.body().contains("7159968480"));
    assertFalse(message.body().contains("uuid-interno"));
  }

  @Test
  void maskHandlesShortAndMissingValues() {
    assertEquals("", EmailMessageComposer.mask(null));
    assertEquals("***", EmailMessageComposer.mask("123"));
    assertEquals("******8480", EmailMessageComposer.mask("7159968480"));
  }

  @Test
  void escapesHtmlComingFromTheEvent() {
    final EmailMessage message =
        composer.compose(NotificationType.USER_CREATED, "<b>Ana</b>", Map.of());

    assertFalse(message.body().contains("<b>Ana</b>"));
    assertTrue(message.body().contains("&lt;b&gt;Ana&lt;/b&gt;"));
  }

  @Test
  void escapeHandlesNullAndSpecialCharacters() {
    assertEquals("", EmailMessageComposer.escape(null));
    assertEquals("&amp;&lt;&gt;&quot;&#39;", EmailMessageComposer.escape("&<>\"'"));
  }
}