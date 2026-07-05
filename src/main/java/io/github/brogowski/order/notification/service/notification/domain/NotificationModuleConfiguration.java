package io.github.brogowski.order.notification.service.notification.domain;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
class NotificationModuleConfiguration {

    @Bean
    NotificationFacade notificationFacade(JdbcClient jdbcClient, Clock clock) {
        final EmailMessageFactory emailMessageFactory = new EmailMessageFactory();
        final MockEmailSender emailSender = new MockEmailSender();
        final JdbcNotificationLogRepository notificationLogRepository = new JdbcNotificationLogRepository(jdbcClient);
        return new NotificationFacade(emailMessageFactory, emailSender, notificationLogRepository, clock);
    }
}
