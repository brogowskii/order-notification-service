package io.github.brogowski.order.notification.service.orderaudit.config;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

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
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.putAll(kafkaProperties.getProperties());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        properties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderReceivedMessage.class.getName());
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "io.github.brogowski.order.notification.service.messaging");
        properties.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    DefaultErrorHandler orderReceivedKafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.consumers.order-audit.dead-letter-topic}") String deadLetterTopic,
            @Value("${app.kafka.consumers.retry.initial-interval}") Duration initialInterval,
            @Value("${app.kafka.consumers.retry.max-interval}") Duration maxInterval,
            @Value("${app.kafka.consumers.retry.max-elapsed-time}") Duration maxElapsedTime) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, exception) -> new TopicPartition(deadLetterTopic, record.partition()));
        ExponentialBackOff backOff = new ExponentialBackOff(initialInterval.toMillis(), 2.0);
        backOff.setMaxInterval(maxInterval.toMillis());
        backOff.setMaxElapsedTime(maxElapsedTime.toMillis());
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
