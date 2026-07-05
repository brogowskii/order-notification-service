package io.github.brogowski.order.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "spring.flyway.enabled=false",
            "spring.kafka.listener.auto-startup=false",
            "app.notification-outbox.enabled=false"
        })
class OrderNotificationServiceApplicationTests {

    @Test
    void contextLoads() {}
}
