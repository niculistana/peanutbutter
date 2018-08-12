package com.somamission.peanutbutter.config;

import com.somamission.peanutbutter.impl.UserService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class RedisConfig {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Bean
    public RedissonClient redissonClient() {
        try {
            return configureRedissionClient();
        } catch (IOException e) {
            // FIXME: how do I handle bean-level exceptions??
            // also, handle master-slave redis configuration when RedisConnectionException gets thrown
            e.printStackTrace();
            logger.error("Unable to set-up redisson client. Reason: " + e.getMessage());
            return null;
        }
    }

    private RedissonClient configureRedissionClient() throws IOException {
        File redisConfigFile = new File(RedisConfig.class.getClassLoader().getResource("redis-config.yaml").getFile());
        Config config = Config.fromYAML(redisConfigFile);
        return Redisson.create(config);
    }
}
