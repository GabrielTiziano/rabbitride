package com.gghiaroni.rabbitride.rentalservice.integration.car.exception;

public class CarroIndisponivelException extends RuntimeException {

    public CarroIndisponivelException() {
        super("O carro selecionado não está mais disponível para reserva.");
    }
}
