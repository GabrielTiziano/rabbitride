package com.gghiaroni.rabbitride.notificationservice.messaging;

import com.gghiaroni.rabbitride.commons.events.RentalConfirmedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.notificationservice.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RentalConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RentalConfirmedConsumer.class);
    private static final String CONSUMER_NAME = "notification-service.RentalConfirmedConsumer";
    private static final String TEMPLATE = "email/rental-confirmed";

    private final EmailSender emailSender;

    public RentalConfirmedConsumer(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = Queues.NOTIFICATION_CONFIRMED)
    public void onRentalConfirmed(RentalConfirmedEvent event) {
        log.info("Recebido RentalConfirmed: eventId={}, rentalId={}, userEmail={}",
            event.eventId(), event.rentalId(), event.userEmail());

        emailSender.sendHtml(
            event.userEmail(),
            "Sua reserva foi confirmada",
            TEMPLATE,
            Map.of(
                "rentalId", event.rentalId(),
                "carroDescricao", event.carroDescricao()
            )
        );
    }
}
