package io.github.brogowski.order.notification.service.orderaudit.domain;

import io.github.brogowski.order.notification.service.notificationoutbox.domain.NotificationOutboxFacade;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
class OrderAuditModuleConfiguration {

    @Bean
    OrderAuditFacade orderAuditFacade(
            JdbcClient jdbcClient, NotificationOutboxFacade notificationOutboxFacade, Clock clock) {
        final JdbcOrderRequestAuditRepository orderRequestAuditRepository =
                new JdbcOrderRequestAuditRepository(jdbcClient);
        return new OrderAuditFacade(orderRequestAuditRepository, notificationOutboxFacade, clock);
    }
}
