package com.gghiaroni.rabbitride.analysisservice;

import com.gghiaroni.rabbitride.analysisservice.analysis.Blacklist;
import com.gghiaroni.rabbitride.analysisservice.analysis.BlacklistRepository;
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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AnalysisFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit =
        new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    BlacklistRepository blacklistRepository;

    @BeforeEach
    void limpar() {
        blacklistRepository.deleteAll();
        rabbitAdmin.purgeQueue(Queues.ANALYSIS_REQUESTED, true);
        rabbitAdmin.purgeQueue(Queues.RENTAL_ANALYSIS_COMPLETED, true);
    }

    @Test
    void deveAprovarQuandoCpfNaoEstaNaBlacklist() {
        // given
        UUID rentalId = UUID.randomUUID();
        RentalRequestedEvent event = RentalRequestedEvent.of(
            rentalId,
            UUID.randomUUID(),
            "user@test.com",
            "99988877766",                  // CPF não bloqueado
            UUID.randomUUID()
        );

        // when
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );

        // then
        Object received = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_ANALYSIS_COMPLETED,
            10_000
        );

        assertThat(received)
            .as("Esperava mensagem AnalysisCompleted na queue")
            .isNotNull()
            .isInstanceOf(AnalysisCompletedEvent.class);

        AnalysisCompletedEvent completed = (AnalysisCompletedEvent) received;
        assertThat(completed.rentalId()).isEqualTo(rentalId);
        assertThat(completed.resultado())
            .isEqualTo(AnalysisCompletedEvent.Resultado.APPROVED);
        assertThat(completed.motivo()).isNull();
    }

    @Test
    void deveRejeitarQuandoCpfEstaNaBlacklist() {
        // given
        String cpfBloqueado = "12345678901";
        String motivoBloqueio = "Teste de integração — CPF bloqueado";
        blacklistRepository.save(new Blacklist(cpfBloqueado, motivoBloqueio));

        UUID rentalId = UUID.randomUUID();
        RentalRequestedEvent event = RentalRequestedEvent.of(
            rentalId,
            UUID.randomUUID(),
            "user@test.com",
            cpfBloqueado,                   // CPF na blacklist
            UUID.randomUUID()
        );

        // when
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );

        // then
        Object received = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_ANALYSIS_COMPLETED,
            10_000
        );

        assertThat(received)
            .as("Esperava mensagem AnalysisCompleted na queue")
            .isNotNull()
            .isInstanceOf(AnalysisCompletedEvent.class);

        AnalysisCompletedEvent completed = (AnalysisCompletedEvent) received;
        assertThat(completed.rentalId()).isEqualTo(rentalId);
        assertThat(completed.resultado())
            .isEqualTo(AnalysisCompletedEvent.Resultado.REJECTED);
        assertThat(completed.motivo()).isEqualTo(motivoBloqueio);
    }
}
