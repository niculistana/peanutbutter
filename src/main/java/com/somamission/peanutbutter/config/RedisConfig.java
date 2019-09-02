package com.somamission.peanutbutter.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Configuration
public class RedisConfig {
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedissonClient redissonClient() {
        try {
            return configureRedissionClient();
        } catch (IOException e) {
            logger.error("Unable to set-up redisson client.");
            return null;
        }
    }

    private RedissonClient configureRedissionClient() throws IOException {
        File redisConfigFile = new File(Objects.requireNonNull(RedisConfig.class.getClassLoader().getResource("redis-config.yaml")).getFile());
        Config config = Config.fromYAML(redisConfigFile);
        return Redisson.create(config);
    }
}
