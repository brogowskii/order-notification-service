package io.github.brogowski.order.notification.service.notification.domain;

record EmailMessage(String recipientEmail, String subject, String body) {}
