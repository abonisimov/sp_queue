package net.alex.game.queue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "executor")
public record ExecutorConfig(int poolSize) {}
