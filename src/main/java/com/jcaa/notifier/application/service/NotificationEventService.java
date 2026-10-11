package com.jcaa.notifier.application.service;

import com.jcaa.notifier.application.port.in.ProcessNotificationEventUseCase;
import com.jcaa.notifier.application.port.out.SendNotificationPort;
import com.jcaa.notifier.application.service.dto.command.NotificationEventCommand;
import com.jcaa.notifier.domain.enums.NotificationType;
import com.jcaa.notifier.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventService implements ProcessNotificationEventUseCase {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventService.class);

  private final SendNotificationPort sendNotificationPort;
  private final EmailMessageComposer messageComposer;
  private final String senderEmail;

  public NotificationEventService(
      final SendNotificationPort sendNotificationPort,
      final EmailMessageComposer messageComposer,
      @Value("${notifier.sender.email:no-reply@example.com}") final String senderEmail) {
    this.sendNotificationPort = sendNotificationPort;
    this.messageComposer = messageComposer;
    this.senderEmail = senderEmail;
  }

  @Override
  public void execute(final NotificationEventCommand command) {
    final NotificationType type = NotificationType.fromString(command.type());
    final EmailMessage message =
        messageComposer.compose(type, command.recipientName(), command.data());

    final Notification notification =
        Notification.create(
            command.eventId(),
            command.type(),
            senderEmail,
            command.recipientEmail(),
            command.recipientName(),
            message.subject(),
            message.body());

    sendNotificationPort.send(notification);
    log.info("Notificación {} ({}) entregada.", notification.id().value(), notification.type());
  }
}