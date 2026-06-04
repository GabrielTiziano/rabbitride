package com.gghiaroni.rabbitride.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Fluxo completo: register seguido de login deve retornar token JWT")
    void fluxoCompletoRegisterEntaoLoginDeveRetornarToken() throws Exception {
        Map<String, String> registerPayload = Map.of(
            "nome", "Gabriel Teste",
            "email", "gabriel.integration@test.com",
            "senha", "senha123456",
            "cpf", "52998224725"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerPayload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("gabriel.integration@test.com"))
            .andExpect(jsonPath("$.nome").value("Gabriel Teste"));

        Map<String, String> loginPayload = Map.of(
            "email", "gabriel.integration@test.com",
            "senha", "senha123456"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Register com payload inválido deve retornar 400 com ProblemDetail")
    void registerComPayloadInvalidoDeveRetornar400() throws Exception {
        Map<String, String> payloadInvalido = Map.of(
            "nome", "",
            "email", "não-é-email",
            "senha", "123",
            "cpf", "111"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadInvalido)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://rabbitride.com/errors/validation"))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.senha").exists());
    }

    @Test
    @DisplayName("Login com senha errada deve retornar 401")
    void loginComSenhaErradaDeveRetornar401() throws Exception {
        Map<String, String> registerPayload = Map.of(
            "nome", "Outro Teste",
            "email", "outro.integration@test.com",
            "senha", "senha123456",
            "cpf", "11144477735"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerPayload)))
            .andExpect(status().isCreated());

        Map<String, String> loginPayload = Map.of(
            "email", "outro.integration@test.com",
            "senha", "senhaERRADA"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Não autenticado"))
            .andExpect(jsonPath("$.detail").value("Credenciais inválidas."));
    }

    @Test
    @DisplayName("Register de email duplicado deve retornar 409 com ProblemDetail")
    void registerDuplicadoDeveRetornar409() throws Exception {
        Map<String, String> payload = Map.of(
            "nome", "Duplicado",
            "email", "duplicado.integration@test.com",
            "senha", "senha123456",
            "cpf", "15350946056"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").value("https://rabbitride.com/errors/email-already-exists"))
            .andExpect(jsonPath("$.detail").value(containsString("duplicado.integration@test.com")));
    }
}
