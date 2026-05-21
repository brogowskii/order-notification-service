package io.github.brogowski.order.notification.service.notification;

interface EmailSender {

  void send(EmailMessage message);
}
