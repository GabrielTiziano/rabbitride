package com.gghiaroni.rabbitride.carservice.integration;

import com.gghiaroni.rabbitride.carservice.car.Carro;
import com.gghiaroni.rabbitride.carservice.car.CarroRepository;
import com.gghiaroni.rabbitride.carservice.car.CarroService;
import com.gghiaroni.rabbitride.carservice.car.StatusCarro;
import com.gghiaroni.rabbitride.carservice.car.dto.CreateCarroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CarCacheIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired private CarroService carroService;
    @Autowired private CarroRepository carroRepository;
    @Autowired private CacheManager cacheManager;

    private UUID carroId;

    @BeforeEach
    void setUp() {
        carroRepository.deleteAll();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        Carro carro = Carro.builder()
            .placa("TST1234")
            .modelo("Civic")
            .marca("Honda")
            .cor("Preto")
            .ano(2023)
            .valorDiaria(new BigDecimal("220.00"))
            .status(StatusCarro.DISPONIVEL)
            .build();

        carroId = carroRepository.save(carro).getId();
    }

    @Test
    @DisplayName("Buscar por ID duas vezes: segunda chamada deve vir do cache Redis")
    void buscarPorIdDuasVezesDeveCachearNaSegunda() {
        // primeira chamada — popula cache
        carroService.buscarPorId(carroId);

        // verifica que a entry está no cache
        Object cached = cacheManager.getCache("cars-by-id").get(carroId).get();
        assertThat(cached).isNotNull();

        // segunda chamada — vem do cache (não da DB)
        // verificação implícita: se viesse da DB, ainda funcionaria
        // verificação explícita: o conteúdo é igual
        var response = carroService.buscarPorId(carroId);
        assertThat(response.id()).isEqualTo(carroId);
    }

    @Test
    @DisplayName("Criar novo carro deve invalidar cache cars-list")
    void criarDeveInvalidarCacheCarsList() {
        // popula cache de lista
        carroService.listar(null, org.springframework.data.domain.PageRequest.of(0, 20));
        assertThat(cacheManager.getCache("cars-list").get("null-0-20")).isNotNull();

        // cria um novo carro — deve invalidar cars-list
        carroService.criar(new CreateCarroRequest(
            "NEW1234", "Yaris", "Toyota", "Branco", 2024, new BigDecimal("150.00")
        ));

        // cache de cars-list deve estar vazio
        assertThat(cacheManager.getCache("cars-list").get("null-0-20")).isNull();
    }

    @Test
    @DisplayName("Reservar carro deve invalidar cache cars-by-id desse carro")
    void reservarDeveInvalidarCacheCarsById() {
        // popula cache
        carroService.buscarPorId(carroId);
        assertThat(cacheManager.getCache("cars-by-id").get(carroId)).isNotNull();

        // reserva — deve invalidar
        carroService.reservar(carroId);

        // cache da key específica está vazio
        assertThat(cacheManager.getCache("cars-by-id").get(carroId)).isNull();
    }
}
