package com.gghiaroni.rabbitride.analysisservice;

import com.gghiaroni.rabbitride.analysisservice.analysis.AnalysisResult;
import com.gghiaroni.rabbitride.analysisservice.analysis.AnalysisService;
import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class AnalysisRetryAndDlqTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit =
        new RabbitMQContainer("rabbitmq:3-management-alpine");

    @MockBean
    AnalysisService analysisService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @BeforeEach
    void limpar() {
        reset(analysisService);
        rabbitAdmin.purgeQueue(Queues.ANALYSIS_REQUESTED, true);
        rabbitAdmin.purgeQueue(Queues.RENTAL_ANALYSIS_COMPLETED, true);
        rabbitAdmin.purgeQueue(Queues.RENTAL_DLQ, true);
    }

    @Test
    void deveRecuperarApósRetryQuandoFalhaTransiente() {
        // given — serviço falha na 1ª chamada e passa na 2ª
        when(analysisService.analisar(any(RentalRequestedEvent.class)))
            .thenThrow(new RuntimeException("Falha transiente simulada"))
            .thenReturn(AnalysisResult.aprovar());

        UUID rentalId = UUID.randomUUID();
        RentalRequestedEvent event = RentalRequestedEvent.of(
            rentalId,
            UUID.randomUUID(),
            "user@test.com",
            "12345678901",
            UUID.randomUUID()
        );

        // when
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );

        // then — mensagem deve chegar na queue de saída (15s pra cobrir backoff)
        Object received = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_ANALYSIS_COMPLETED,
            15_000
        );

        assertThat(received)
            .as("Esperava AnalysisCompleted após retry bem-sucedido")
            .isNotNull()
            .isInstanceOf(AnalysisCompletedEvent.class);

        AnalysisCompletedEvent completed = (AnalysisCompletedEvent) received;
        assertThat(completed.rentalId()).isEqualTo(rentalId);
        assertThat(completed.resultado())
            .isEqualTo(AnalysisCompletedEvent.Resultado.APPROVED);

        // O service foi chamado 2x: 1 falha + 1 sucesso
        verify(analysisService, times(2)).analisar(any(RentalRequestedEvent.class));
    }

    @Test
    void deveIrParaDlqAposEsgotarRetries() {
        // given — toda chamada falha
        when(analysisService.analisar(any(RentalRequestedEvent.class)))
            .thenThrow(new RuntimeException("Falha permanente simulada"));

        UUID rentalId = UUID.randomUUID();
        RentalRequestedEvent event = RentalRequestedEvent.of(
            rentalId,
            UUID.randomUUID(),
            "user@test.com",
            "12345678901",
            UUID.randomUUID()
        );

        // when
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );

        // then — mensagem deve ir parar no DLQ depois de esgotar retries
        Object dlqMessage = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_DLQ,
            15_000
        );

        assertThat(dlqMessage)
            .as("Esperava mensagem original no DLQ após esgotar retries")
            .isNotNull()
            .isInstanceOf(RentalRequestedEvent.class);

        RentalRequestedEvent dlqEvent = (RentalRequestedEvent) dlqMessage;
        assertThat(dlqEvent.rentalId()).isEqualTo(rentalId);

        // Service foi chamado 3x (max-attempts)
        verify(analysisService, times(3)).analisar(any(RentalRequestedEvent.class));
    }
}
