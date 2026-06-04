package com.gghiaroni.rabbitride.userservice.user;

import com.gghiaroni.rabbitride.userservice.user.dto.RegisterRequest;
import com.gghiaroni.rabbitride.userservice.user.dto.RegisterResponse;
import com.gghiaroni.rabbitride.userservice.user.exception.CpfAlreadyExistsException;
import com.gghiaroni.rabbitride.userservice.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Register deve criar usuário com sucesso")
    void registerCriaUsuarioComSucesso() {
        RegisterRequest request = new RegisterRequest(
            "Gabriel", "gabriel@test.com", "senha123456", "52998224725"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("hash-bcrypt");
        mockSuccessfulSave();

        RegisterResponse response = userService.register(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("gabriel@test.com");
        assertThat(response.nome()).isEqualTo("Gabriel");
    }

    @Test
    @DisplayName("Register deve lançar exception se email já existe")
    void registerLancaExceptionSeEmailJaExiste() {
        RegisterRequest request = new RegisterRequest(
            "Gabriel", "gabriel@test.com", "senha123456", "52998224725"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("gabriel@test.com");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Register deve lançar exception se CPF já existe")
    void registerLancaExceptionSeCpfJaExiste() {
        RegisterRequest request = new RegisterRequest(
            "Gabriel", "gabriel@test.com", "senha123456", "52998224725"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(CpfAlreadyExistsException.class)
            .hasMessageContaining("52998224725");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Register deve normalizar CPF (remover pontuação) antes de checar e salvar")
    void registerNormalizaCpfAntesDeSalvar() {
        RegisterRequest request = new RegisterRequest(
            "Gabriel", "gabriel@test.com", "senha123456", "529.982.247-25"
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByCpf("52998224725")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        mockSuccessfulSave();

        userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getCpf()).isEqualTo("52998224725");
        verify(userRepository).existsByCpf("52998224725");
    }

    @Test
    @DisplayName("Register deve salvar senha criptografada (BCrypt), nunca em texto puro")
    void registerCriptografaSenhaComBCrypt() {
        RegisterRequest request = new RegisterRequest(
            "Gabriel", "gabriel@test.com", "senha123456", "52998224725"
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByCpf(any())).thenReturn(false);
        when(passwordEncoder.encode("senha123456")).thenReturn("$2a$10$hash-bcrypt");
        mockSuccessfulSave();

        userService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User salvo = captor.getValue();
        assertThat(salvo.getSenha()).isEqualTo("$2a$10$hash-bcrypt");
        assertThat(salvo.getSenha()).isNotEqualTo("senha123456");
        verify(passwordEncoder).encode("senha123456");
    }

    private void mockSuccessfulSave() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User input = invocation.getArgument(0);
            return User.builder()
                .id(UUID.randomUUID())
                .nome(input.getNome())
                .email(input.getEmail())
                .senha(input.getSenha())
                .cpf(input.getCpf())
                .criadoEm(Instant.now())
                .build();
        });
    }
}
