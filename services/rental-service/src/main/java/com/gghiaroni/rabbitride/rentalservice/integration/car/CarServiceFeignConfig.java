package com.gghiaroni.rabbitride.rentalservice.integration.car;

import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CarServiceFeignConfig {

    @Bean
    public InternalTokenRequestInterceptor internalTokenRequestInterceptor(
        @Value("${car-service.internal-token}") String token
    ) {
        return new InternalTokenRequestInterceptor(token);
    }

    @Bean
    public ErrorDecoder carServiceErrorDecoder() {
        return new CarServiceErrorDecoder();
    }
}
