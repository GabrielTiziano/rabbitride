package com.gghiaroni.rabbitride.analysisservice.messaging;

import com.gghiaroni.rabbitride.analysisservice.analysis.AnalysisService;
import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisRequestedConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnalysisRequestedConsumer.class);
    private final AnalysisService analysisService;

    public AnalysisRequestedConsumer(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }


    @RabbitListener(queues = Queues.ANALYSIS_REQUESTED)
    public void onRentalRequested(RentalRequestedEvent event) {
        log.info("Recebido RentalRequested: eventId={}, rentalId={}, userId={}, userEmail={}, carroId={}",
            event.eventId(), event.rentalId(), event.userId(), event.userEmail(), event.carroId());
        analysisService.analisar(event);
    }
}
