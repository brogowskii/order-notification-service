package io.github.brogowski.order.notification.service.orderaudit.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.notificationoutbox.domain.NotificationOutboxCommand;
import io.github.brogowski.order.notification.service.notificationoutbox.domain.NotificationOutboxFacade;
import io.github.brogowski.order.notification.service.orderaudit.dto.OrderRequestAuditDto;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class OrderAuditFacade {

    private final JdbcOrderRequestAuditRepository orderRequestAuditRepository;
    private final NotificationOutboxFacade notificationOutboxFacade;
    private final Clock clock;

    OrderAuditFacade(
            JdbcOrderRequestAuditRepository orderRequestAuditRepository,
            NotificationOutboxFacade notificationOutboxFacade,
            Clock clock) {
        this.orderRequestAuditRepository = orderRequestAuditRepository;
        this.notificationOutboxFacade = notificationOutboxFacade;
        this.clock = clock;
    }

    @Transactional
    public void audit(OrderReceivedMessage message) {
        Instant storedAt = Instant.now(clock);
        OrderRequestAudit audit = OrderRequestAudit.from(message, storedAt);

        orderRequestAuditRepository.save(audit);
        notificationOutboxFacade.schedule(new NotificationOutboxCommand(
                audit.requestId(),
                audit.shipmentNumber(),
                audit.recipientEmail(),
                audit.recipientCountryCode(),
                audit.senderCountryCode(),
                audit.statusCode(),
                storedAt));
    }

    public Optional<OrderRequestAuditDto> findByRequestId(UUID requestId) {
        return orderRequestAuditRepository.findByRequestId(requestId).map(OrderRequestAudit::toDto);
    }
}
