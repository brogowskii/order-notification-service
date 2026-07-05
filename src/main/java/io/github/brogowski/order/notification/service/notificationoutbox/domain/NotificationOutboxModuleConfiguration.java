package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class NotificationOutboxModuleConfiguration {

    @Bean
    NotificationOutboxFacade notificationOutboxFacade(JdbcClient jdbcClient) {
        return new NotificationOutboxFacade(new JdbcNotificationOutboxRepository(jdbcClient));
    }

    @Bean
    @ConditionalOnProperty(name = "app.notification-outbox.enabled", havingValue = "true", matchIfMissing = true)
    NotificationOutboxPublisherTask notificationOutboxPublisherTask(
            JdbcClient jdbcClient,
            KafkaTemplate<String, NotificationRequestedMessage> kafkaTemplate,
            Clock clock,
            @Value("${app.kafka.topics.notifications-requested}") String topicName,
            @Value("${app.kafka.publish-timeout}") Duration publishTimeout,
            @Value("${app.notification-outbox.batch-size}") int batchSize,
            @Value("${app.notification-outbox.retry-delay}") Duration retryDelay,
            @Value("${app.notification-outbox.processing-timeout}") Duration processingTimeout,
            @Value("${app.notification-outbox.max-attempts}") int maxAttempts) {
        final JdbcNotificationOutboxRepository notificationOutboxRepository =
                new JdbcNotificationOutboxRepository(jdbcClient);
        final KafkaNotificationRequestedPublisher notificationRequestedPublisher =
                new KafkaNotificationRequestedPublisher(kafkaTemplate, topicName, publishTimeout);
        return new NotificationOutboxPublisherTask(
                notificationOutboxRepository,
                notificationRequestedPublisher,
                clock,
                batchSize,
                retryDelay,
                processingTimeout,
                maxAttempts);
    }
}
