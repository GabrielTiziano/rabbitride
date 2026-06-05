package com.gghiaroni.rabbitride.carservice.car.exception;

import java.util.UUID;

public class CarroNotFoundException extends RuntimeException {
    public CarroNotFoundException(UUID id) {
        super("Carro não encontrado: " + id);
    }
}
