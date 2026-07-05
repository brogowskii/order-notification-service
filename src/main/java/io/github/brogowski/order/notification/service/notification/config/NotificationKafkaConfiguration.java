package io.github.brogowski.order.notification.service.notification.config;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
class NotificationKafkaConfiguration {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedMessage>
            notificationRequestedKafkaListenerContainerFactory(
                    ConsumerFactory<String, NotificationRequestedMessage> notificationRequestedConsumerFactory,
                    @Value("${app.kafka.consumers.notification.concurrency}") int concurrency,
                    @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationRequestedMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationRequestedConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    ConsumerFactory<String, NotificationRequestedMessage> notificationRequestedConsumerFactory(
            KafkaProperties kafkaProperties,
            @Value("${app.kafka.consumers.notification.max-poll-records}") int maxPollRecords) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.putAll(kafkaProperties.getProperties());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        properties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationRequestedMessage.class.getName());
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "io.github.brogowski.order.notification.service.messaging");
        properties.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(properties);
    }
}
