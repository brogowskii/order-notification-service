package io.github.brogowski.order.notification.service.notification.domain;

interface EmailSender {

    void send(EmailMessage message);
}
