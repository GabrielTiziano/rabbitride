package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import com.gghiaroni.rabbitride.carservice.car.dto.CreateCarroRequest;
import com.gghiaroni.rabbitride.carservice.car.exception.CarroNotAvailableException;
import com.gghiaroni.rabbitride.carservice.car.exception.CarroNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @Test
    @DisplayName("Criar deve persistir carro com status DISPONIVEL")
    void criarDevePersistirComStatusDisponivel() {
        CreateCarroRequest request = new CreateCarroRequest(
            "ABC1234", "Civic", "Honda", "Preto", 2023, new BigDecimal("220.00")
        );
        when(carroRepository.save(any(Carro.class))).thenAnswer(this::devolverCarroComoSeFosseSalvo);

        CarroResponse response = carroService.criar(request);

        assertThat(response.status()).isEqualTo("DISPONIVEL");
        assertThat(response.modelo()).isEqualTo("Civic");
    }

    @Test
    @DisplayName("Reservar carro DISPONIVEL deve mudar status para RESERVADO")
    void reservarCarroDisponivelDeveMudarParaReservado() {
        UUID id = UUID.randomUUID();
        Carro disponivel = carroBase(id, StatusCarro.DISPONIVEL);
        when(carroRepository.findById(id)).thenReturn(Optional.of(disponivel));
        when(carroRepository.save(any(Carro.class))).thenAnswer(this::devolverCarroComoSeFosseSalvo);

        CarroResponse response = carroService.reservar(id);

        assertThat(response.status()).isEqualTo("RESERVADO");
    }

    @Test
    @DisplayName("Reservar carro RESERVADO deve lançar CarroNotAvailableException")
    void reservarCarroJaReservadoDeveFalhar() {
        UUID id = UUID.randomUUID();
        Carro reservado = carroBase(id, StatusCarro.RESERVADO);
        when(carroRepository.findById(id)).thenReturn(Optional.of(reservado));

        assertThatThrownBy(() -> carroService.reservar(id))
            .isInstanceOf(CarroNotAvailableException.class);

        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Liberar carro RESERVADO deve mudar status para DISPONIVEL")
    void liberarCarroReservadoDeveMudarParaDisponivel() {
        UUID id = UUID.randomUUID();
        Carro reservado = carroBase(id, StatusCarro.RESERVADO);
        when(carroRepository.findById(id)).thenReturn(Optional.of(reservado));
        when(carroRepository.save(any(Carro.class))).thenAnswer(this::devolverCarroComoSeFosseSalvo);

        CarroResponse response = carroService.liberar(id);

        assertThat(response.status()).isEqualTo("DISPONIVEL");
    }

    @Test
    @DisplayName("Liberar carro DISPONIVEL (não reservado) deve lançar CarroNotAvailableException")
    void liberarCarroNaoReservadoDeveFalhar() {
        UUID id = UUID.randomUUID();
        Carro disponivel = carroBase(id, StatusCarro.DISPONIVEL);
        when(carroRepository.findById(id)).thenReturn(Optional.of(disponivel));

        assertThatThrownBy(() -> carroService.liberar(id))
            .isInstanceOf(CarroNotAvailableException.class);

        verify(carroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Buscar por ID inexistente deve lançar CarroNotFoundException")
    void buscarPorIdInexistenteDeveFalhar() {
        UUID id = UUID.randomUUID();
        when(carroRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carroService.buscarPorId(id))
            .isInstanceOf(CarroNotFoundException.class);
    }

    private Carro carroBase(UUID id, StatusCarro status) {
        return Carro.builder()
            .id(id)
            .placa("ABC1234")
            .modelo("Civic")
            .marca("Honda")
            .cor("Preto")
            .ano(2023)
            .valorDiaria(new BigDecimal("220.00"))
            .status(status)
            .versao(0L)
            .criadoEm(Instant.now())
            .build();
    }

    private Carro devolverCarroComoSeFosseSalvo(org.mockito.invocation.InvocationOnMock invocation) {
        Carro input = invocation.getArgument(0);
        return Carro.builder()
            .id(input.getId() != null ? input.getId() : UUID.randomUUID())
            .placa(input.getPlaca())
            .modelo(input.getModelo())
            .marca(input.getMarca())
            .cor(input.getCor())
            .ano(input.getAno())
            .valorDiaria(input.getValorDiaria())
            .status(input.getStatus())
            .versao(input.getVersao() != null ? input.getVersao() : 0L)
            .criadoEm(input.getCriadoEm() != null ? input.getCriadoEm() : Instant.now())
            .build();
    }
}
