package com.gghiaroni.rabbitride.carservice.car;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarroRepository extends JpaRepository<Carro, UUID> {
    List<Carro> findByStatus(StatusCarro status);
    Page<Carro> findByStatus(StatusCarro status, Pageable pageable);
    Optional<Carro> findByPlaca(String placa);
}
