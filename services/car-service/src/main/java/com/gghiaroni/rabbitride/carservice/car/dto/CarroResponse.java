package com.gghiaroni.rabbitride.carservice.car.dto;

import com.gghiaroni.rabbitride.carservice.car.Carro;

import java.math.BigDecimal;
import java.util.UUID;

public record CarroResponse (
    UUID id,
    String placa,
    String modelo,
    String marca,
    String cor,
    Integer ano,
    BigDecimal valorDiaria,
    String status,
    String statusDescricao
){
    public static CarroResponse from(Carro carro) {
        return new CarroResponse(
            carro.getId(),
            carro.getPlaca(),
            carro.getModelo(),
            carro.getMarca(),
            carro.getCor(),
            carro.getAno(),
            carro.getValorDiaria(),
            carro.getStatus().name(),
            carro.getStatus().descricao()
        );
    }
}
