package com.gghiaroni.rabbitride.rentalservice.integration.car;

import java.math.BigDecimal;
import java.util.UUID;

public record CarroResponse(
    UUID id,
    String placa,
    String modelo,
    String marca,
    String cor,
    Integer ano,
    BigDecimal valorDiaria,
    String status,
    String statusDescricao
) {}
