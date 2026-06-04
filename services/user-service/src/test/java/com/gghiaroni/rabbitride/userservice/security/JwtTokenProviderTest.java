package com.gghiaroni.rabbitride.userservice.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gghiaroni.rabbitride.userservice.user.User;
import com.gghiaroni.rabbitride.userservice.user.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "secret-de-teste-com-pelo-menos-256-bits-aaaaaaaaaaaaaaaa";
    private static final long EXPIRATION_MS = 3_600_000L; // 1h

    private JwtTokenProvider provider;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);

        User user = User.builder()
            .id(UUID.randomUUID())
            .nome("Gabriel")
            .email("gabriel@test.com")
            .senha("hash-fake")
            .cpf("12345678901")
            .criadoEm(Instant.now())
            .build();

        principal = new UserPrincipal(user);
    }

    @Test
    @DisplayName("Token gerado deve ser válido")
    void tokenGeradoDeveSerValido() {
        String token = provider.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("Email extraído do token deve bater com o do principal")
    void emailExtraidoDeveBaterComOPrincipal() {
        String token = provider.generateToken(principal);

        String emailExtraido = provider.getEmailFromToken(token);

        assertThat(emailExtraido).isEqualTo(principal.getEmail());
    }

    @Test
    @DisplayName("Token mal-formado deve ser inválido")
    void tokenMalFormadoDeveSerInvalido() {
        String tokenFake = "isso.nao.eh.um.jwt";

        assertThat(provider.isValid(tokenFake)).isFalse();
    }

    @Test
    @DisplayName("Token com assinatura adulterada deve ser inválido")
    void tokenComAssinaturaAdulteradaDeveSerInvalido() {
        String tokenValido = provider.generateToken(principal);
        String tokenAdulterado = tokenValido.substring(0, tokenValido.length() - 5) + "XXXXX";

        assertThat(provider.isValid(tokenAdulterado)).isFalse();
    }

    @Test
    @DisplayName("Token expirado deve ser inválido e lançar exception ao extrair claims")
    void tokenExpiradoDeveSerInvalido() {
        JwtTokenProvider providerExpirado = new JwtTokenProvider(SECRET, -1000L);
        String tokenExpirado = providerExpirado.generateToken(principal);

        assertThat(providerExpirado.isValid(tokenExpirado)).isFalse();
        assertThatThrownBy(() -> providerExpirado.getEmailFromToken(tokenExpirado))
            .isInstanceOf(JWTVerificationException.class);
    }
}
