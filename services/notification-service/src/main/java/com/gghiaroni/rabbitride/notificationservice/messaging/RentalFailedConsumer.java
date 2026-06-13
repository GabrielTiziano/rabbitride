package com.gghiaroni.rabbitride.notificationservice.messaging;

import com.gghiaroni.rabbitride.commons.events.RentalFailedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.notificationservice.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RentalFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RentalFailedConsumer.class);
    private static final String CONSUMER_NAME = "notification-service.RentalFailedConsumer";
    private static final String TEMPLATE = "email/rental-failed";

    private final EmailSender emailSender;

    public RentalFailedConsumer(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = Queues.NOTIFICATION_FAILED)
    public void onRentalFailed(RentalFailedEvent event) {
        log.info("Recebido RentalFailed: eventId={}, rentalId={}, userEmail={}, motivo={}",
            event.eventId(), event.rentalId(), event.userEmail(), event.motivo());

        emailSender.sendHtml(
            event.userEmail(),
            "Não foi possível confirmar sua reserva",
            TEMPLATE,
            Map.of(
                "rentalId", event.rentalId(),
                "motivo", event.motivo()
            )
        );
    }
}
