package com.ssafy.arena.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitialTurnGenerationExecutorConfig {
    @Bean(name = "initialTurnGenerationExecutor", destroyMethod = "shutdown")
    public ExecutorService initialTurnGenerationExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
