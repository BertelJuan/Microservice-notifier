package com.jcaa.notifier.application.port.in;

import com.jcaa.notifier.application.service.dto.command.NotificationEventCommand;

/** Puerto de entrada: procesa un evento y entrega la notificación correspondiente. */
public interface ProcessNotificationEventUseCase {

  void execute(NotificationEventCommand command);
}