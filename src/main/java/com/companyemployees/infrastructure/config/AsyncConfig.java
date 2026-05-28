package com.companyemployees.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita @Async y expone un TaskExecutor dedicado ("applicationTaskExecutor").
 * <p>
 * IMPORTANTE: este ejecutor delega trabajo a un pool de hilos, pero NO convierte
 * a Spring MVC ni a Spring Data MongoDB en no bloqueantes. El I/O contra MongoDB
 * sigue siendo bloqueante; @Async + CompletableFuture solo mueve la espera a otro
 * hilo. La alternativa reactiva real seria WebFlux + ReactiveMongoRepository, que
 * no se adopta para conservar la arquitectura y el alcance del proyecto.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "applicationTaskExecutor")
    public Executor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("app-async-");
        executor.initialize();
        return executor;
    }
}
