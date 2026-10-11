package com.jcaa.notifier.domain.model;

import com.jcaa.notifier.domain.enums.NotificationType;
import com.jcaa.notifier.domain.valueobject.DestinationEmail;
import com.jcaa.notifier.domain.valueobject.DestinationName;
import com.jcaa.notifier.domain.valueobject.NotificationContent;
import com.jcaa.notifier.domain.valueobject.NotificationId;
import com.jcaa.notifier.domain.valueobject.NotificationSubject;
import com.jcaa.notifier.domain.valueobject.SenderEmail;
import java.util.Objects;

/** Correo que el notificador debe entregar. Inmutable; se valida al construirse. */
public record Notification(
    NotificationId id,
    NotificationType type,
    SenderEmail sender,
    DestinationEmail destinationEmail,
    DestinationName destinationName,
    NotificationSubject subject,
    NotificationContent content) {

  public Notification {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(sender, "sender");
    Objects.requireNonNull(destinationEmail, "destinationEmail");
    Objects.requireNonNull(destinationName, "destinationName");
    Objects.requireNonNull(subject, "subject");
    Objects.requireNonNull(content, "content");
  }

  public static Notification create(
      final String id,
      final String type,
      final String senderEmail,
      final String destinationEmail,
      final String destinationName,
      final String subject,
      final String content) {
    return new Notification(
        new NotificationId(id),
        NotificationType.fromString(type),
        new SenderEmail(senderEmail),
        new DestinationEmail(destinationEmail),
        new DestinationName(destinationName),
        new NotificationSubject(subject),
        new NotificationContent(content));
  }
}