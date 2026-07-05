package io.github.brogowski.order.notification.service.orderaudit.config;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
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
class OrderAuditKafkaConfiguration {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderReceivedMessage> orderReceivedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderReceivedMessage> orderReceivedConsumerFactory,
            @Qualifier("orderReceivedKafkaErrorHandler") DefaultErrorHandler orderReceivedKafkaErrorHandler,
            @Value("${app.kafka.consumers.order-audit.concurrency}") int concurrency,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        ConcurrentKafkaListenerContainerFactory<String, OrderReceivedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderReceivedConsumerFactory);
        factory.setCommonErrorHandler(orderReceivedKafkaErrorHandler);
        factory.setConcurrency(concurrency);
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    ConsumerFactory<String, OrderReceivedMessage> orderReceivedConsumerFactory(
            KafkaProperties kafkaProperties,
            @Value("${app.kafka.consumers.order-audit.max-poll-records}") int maxPollRecords) {
        return new DefaultKafkaConsumerFactory<>(KafkaListenerConfigurationSupport.consumerProperties(
                kafkaProperties, maxPollRecords, OrderReceivedMessage.class));
    }

    @Bean
    DefaultErrorHandler orderReceivedKafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.consumers.order-audit.dead-letter-topic}") String deadLetterTopic,
            @Value("${app.kafka.consumers.retry.initial-interval}") Duration initialInterval,
            @Value("${app.kafka.consumers.retry.max-interval}") Duration maxInterval,
            @Value("${app.kafka.consumers.retry.max-elapsed-time}") Duration maxElapsedTime) {
        return KafkaListenerConfigurationSupport.deadLetterErrorHandler(
                kafkaTemplate, deadLetterTopic, initialInterval, maxInterval, maxElapsedTime);
    }
}
