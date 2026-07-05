package io.github.brogowski.order.notification.service.notification.config;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.shared.KafkaListenerConfigurationSupport;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
class NotificationKafkaConfiguration {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedMessage>
            notificationRequestedKafkaListenerContainerFactory(
                    ConsumerFactory<String, NotificationRequestedMessage> notificationRequestedConsumerFactory,
                    @Qualifier("notificationRequestedKafkaErrorHandler")
                            DefaultErrorHandler notificationRequestedKafkaErrorHandler,
                    @Value("${app.kafka.consumers.notification.concurrency}") int concurrency,
                    @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationRequestedConsumerFactory);
        factory.setCommonErrorHandler(notificationRequestedKafkaErrorHandler);
        factory.setConcurrency(concurrency);
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    ConsumerFactory<String, NotificationRequestedMessage> notificationRequestedConsumerFactory(
            KafkaProperties kafkaProperties,
            @Value("${app.kafka.consumers.notification.max-poll-records}") int maxPollRecords) {
        return new DefaultKafkaConsumerFactory<>(KafkaListenerConfigurationSupport.consumerProperties(
                kafkaProperties, maxPollRecords, NotificationRequestedMessage.class));
    }

    @Bean
    DefaultErrorHandler notificationRequestedKafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.consumers.notification.dead-letter-topic}") String deadLetterTopic,
            @Value("${app.kafka.consumers.retry.initial-interval}") Duration initialInterval,
            @Value("${app.kafka.consumers.retry.max-interval}") Duration maxInterval,
            @Value("${app.kafka.consumers.retry.max-elapsed-time}") Duration maxElapsedTime) {
        return KafkaListenerConfigurationSupport.deadLetterErrorHandler(
                kafkaTemplate, deadLetterTopic, initialInterval, maxInterval, maxElapsedTime);
    }
}
