package io.github.brogowski.order.notification.service.notification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class MockEmailSender implements EmailSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockEmailSender.class);

  @Override
  public void send(EmailMessage message) {
    LOGGER.info(
        "Mock email sent to {} with subject '{}'", message.recipientEmail(), message.subject());
  }
}
