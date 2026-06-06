package com.gghiaroni.rabbitride.carservice.integration;

import com.gghiaroni.rabbitride.carservice.car.Carro;
import com.gghiaroni.rabbitride.carservice.car.CarroRepository;
import com.gghiaroni.rabbitride.carservice.car.CarroService;
import com.gghiaroni.rabbitride.carservice.car.StatusCarro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CarConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @org.springframework.test.context.DynamicPropertySource
    static void redisProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired private CarroService carroService;
    @Autowired private CarroRepository carroRepository;

    @Test
    @DisplayName("Duas reservas concorrentes no mesmo carro: apenas uma vence (optimistic locking)")
    void duasReservasConcorrentesNoMesmoCarroApenasUmaVence() throws Exception {
        // -------- Arrange --------
        Carro carro = Carro.builder()
            .placa("RAC1234")
            .modelo("Civic")
            .marca("Honda")
            .cor("Preto")
            .ano(2023)
            .valorDiaria(new BigDecimal("220.00"))
            .status(StatusCarro.DISPONIVEL)
            .build();

        UUID id = carroRepository.save(carro).getId();

        int numThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startGate = new CountDownLatch(1);     // segura as threads
        CountDownLatch doneGate = new CountDownLatch(numThreads); // espera todas terminarem
        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        // -------- Act --------
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();           // espera o "tiro de largada"
                    carroService.reservar(id);   // ambas tentam ao mesmo tempo
                    sucessos.incrementAndGet();
                } catch (Exception ex) {
                    // OptimisticLockingFailureException OU CarroNotAvailableException
                    // dependendo da timing, qualquer uma das duas é falha esperada
                    falhas.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();                                    // dispara!
        boolean terminou = doneGate.await(10, TimeUnit.SECONDS);  // 10s de timeout
        executor.shutdown();

        // -------- Assert --------
        assertThat(terminou).as("threads não terminaram a tempo").isTrue();
        assertThat(sucessos.get()).as("exatamente 1 thread deve ter sucesso").isEqualTo(1);
        assertThat(falhas.get()).as("exatamente 1 thread deve falhar").isEqualTo(1);

        // estado final do carro: deve estar RESERVADO (uma das threads venceu)
        Carro depois = carroRepository.findById(id).orElseThrow();
        assertThat(depois.getStatus()).isEqualTo(StatusCarro.RESERVADO);
        assertThat(depois.getVersao()).isEqualTo(1L);  // versão incrementada de 0 → 1
    }
}
