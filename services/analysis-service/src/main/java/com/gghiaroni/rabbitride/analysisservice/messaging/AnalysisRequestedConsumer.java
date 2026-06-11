package com.gghiaroni.rabbitride.analysisservice.messaging;

import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class AnalysisRequestedConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnalysisRequestedConsumer.class);

    @RabbitListener(queues = Queues.ANALYSIS_REQUESTED)
    public void onRentalRequested(RentalRequestedEvent event) {
        log.info("Recebido RentalRequested: eventId={}, rentalId={}, userId={}, userEmail={}, carroId={}",
            event.eventId(), event.rentalId(), event.userId(), event.userEmail(), event.carroId());
    }
}
