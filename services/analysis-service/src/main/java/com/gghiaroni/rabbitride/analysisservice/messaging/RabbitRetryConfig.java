package com.gghiaroni.rabbitride.analysisservice.messaging;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitRetryConfig {

    private final int maxAttempts;
    private final long initialIntervalMs;
    private final double multiplier;
    private final long maxIntervalMs;

    public RabbitRetryConfig(
        @Value("${app.retry.max-attempts}") int maxAttempts,
        @Value("${app.retry.initial-interval-ms}") long initialIntervalMs,
        @Value("${app.retry.multiplier}") double multiplier,
        @Value("${app.retry.max-interval-ms}") long maxIntervalMs
    ) {
        this.maxAttempts = maxAttempts;
        this.initialIntervalMs = initialIntervalMs;
        this.multiplier = multiplier;
        this.maxIntervalMs = maxIntervalMs;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        template.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialIntervalMs);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxIntervalMs);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RetryTemplate retryTemplate) {
        return RetryInterceptorBuilder.stateless()
            .retryOperations(retryTemplate)
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        SimpleRabbitListenerContainerFactoryConfigurer configurer,
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter,
        RetryOperationsInterceptor retryInterceptor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
