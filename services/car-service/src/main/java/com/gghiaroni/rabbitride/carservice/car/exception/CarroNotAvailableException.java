package com.gghiaroni.rabbitride.carservice.car.exception;

import com.gghiaroni.rabbitride.carservice.car.StatusCarro;

import java.util.UUID;

public class CarroNotAvailableException extends RuntimeException {
    public CarroNotAvailableException(UUID id, StatusCarro statusAtual, StatusCarro esperado) {
        super(String.format("Carro %s está %s, esperado %s", id, statusAtual, esperado));
    }
}
