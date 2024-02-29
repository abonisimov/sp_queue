package net.alex.game.queue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serializer")
public record SerializerConfig(String implementation) {}
