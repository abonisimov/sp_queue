package net.alex.game.queue.config;

import net.alex.game.queue.executor.GameThreadPoolExecutor;
import net.alex.game.queue.serialize.EventSerializer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfiguration {

    private final ExecutorConfig executorConfig;
    private final EventSerializer eventSerializer;

    public ThreadPoolConfiguration(ExecutorConfig executorConfig,
                                   SerializerConfig serializerConfig,
                                   BeanFactory beanFactory) {
        this.executorConfig = executorConfig;
        this.eventSerializer = beanFactory.getBean(serializerConfig.implementation(), EventSerializer.class);
    }

    @Bean
    public GameThreadPoolExecutor gameThreadPoolExecutor() {
        return new GameThreadPoolExecutor(
                executorConfig.poolSize(),
                executorConfig.loadFactorPrecision(),
                eventSerializer);
    }
}
