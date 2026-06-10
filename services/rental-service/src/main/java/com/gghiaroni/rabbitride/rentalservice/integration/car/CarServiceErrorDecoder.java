package com.gghiaroni.rabbitride.rentalservice.integration.car;

import com.gghiaroni.rabbitride.rentalservice.integration.car.exception.CarroIndisponivelException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CarServiceErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 409) {
            return new CarroIndisponivelException();
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
