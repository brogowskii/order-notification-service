package io.github.brogowski.order.notification.service.notification;

record EmailMessage(String recipientEmail, String subject, String body) {}
