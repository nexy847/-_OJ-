package com.oj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {
    @Bean
    public ThreadPoolTaskExecutor judgeTaskExecutor(OjProperties properties) {
        int size = Math.max(1, properties.getJudge().getMaxConcurrency());//max为2
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(size);
        executor.setMaxPoolSize(size);
        executor.setQueueCapacity(size * 2);//最多执行四个
        executor.setThreadNamePrefix("judge-");
        executor.initialize();
        return executor;
    }
}
