package com.jcaa.notifier.application.service;

import com.jcaa.notifier.domain.enums.NotificationType;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Arma el asunto y el cuerpo HTML según el tipo de notificación. */
@Component
public class EmailMessageComposer {

  private static final String APP_NAME = "Gestion de Usuarios";

  public EmailMessage compose(
      final NotificationType type, final String recipientName, final Map<String, String> data) {
    final String name = escape(recipientName);
    return switch (type) {
      case USER_CREATED ->
          new EmailMessage(
              "Tu cuenta ha sido creada — " + APP_NAME,
              page(
                  name,
                  "Tu cuenta ha sido creada exitosamente.",
                  item("Rol", data.get("role")),
                  "Te avisaremos por correo cuando tu cuenta esté lista."));
      case USER_UPDATED ->
          new EmailMessage(
              "Tu cuenta ha sido actualizada — " + APP_NAME,
              page(
                  name,
                  "Los datos de tu cuenta fueron actualizados.",
                  item("Rol", data.get("role")) + item("Estado", data.get("status")),
                  "Si no reconoces este cambio, contacta al administrador."));
      case ACCOUNT_CREATED ->
          new EmailMessage(
              "Tu cuenta está lista — " + APP_NAME,
              page(
                  name,
                  "Tu cuenta fue creada y ya puedes usarla.",
                  item("Número de cuenta", mask(data.get("accountNumber"))),
                  "¡Gracias por registrarte!"));
    };
  }

  private static String page(
      final String name, final String intro, final String items, final String footer) {
    return """
        <html>
        <body>
        <h2>Hola, %s</h2>
        <p>%s</p>
        <ul>
        %s</ul>
        <p>%s</p>
        </body>
        </html>
        """
        .formatted(name, intro, items, footer);
  }

  /** Deja visibles solo los últimos 4 dígitos: ******8480. */
  static String mask(final String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    final String clean = value.trim();
    if (clean.length() <= 4) {
      return "*".repeat(clean.length());
    }
    return "*".repeat(clean.length() - 4) + clean.substring(clean.length() - 4);
  }

  private static String item(final String label, final String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return "  <li><strong>" + label + ":</strong> " + escape(value) + "</li>\n";
  }

  /** Evita que texto del evento se interprete como HTML dentro del correo. */
  static String escape(final String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}