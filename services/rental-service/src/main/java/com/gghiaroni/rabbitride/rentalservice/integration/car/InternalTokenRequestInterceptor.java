package com.gghiaroni.rabbitride.rentalservice.integration.car;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class InternalTokenRequestInterceptor implements RequestInterceptor {

    private final String token;

    public InternalTokenRequestInterceptor(String token) {
        this.token = token;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Internal-Token", token);
    }
}
