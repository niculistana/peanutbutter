package com.somamission.peanutbutter.config;

import com.somamission.peanutbutter.constants.CacheConstants;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class SpringCacheConfig {

  @Bean
  public RedissonClient redissonClient(Environment env) {
    Config config = new Config();
    config.useSingleServer().setAddress(env.getProperty("REDIS_URL"));
    return Redisson.create(config);
  }

  @Bean
  CacheManager redisCacheManager(RedissonClient redissonClient) {
    Map<String, CacheConfig> config = new HashMap<>();
    config.put(
        "users", new CacheConfig(CacheConstants.USERS_TTL, CacheConstants.USERS_MAX_IDLE_TIME));
    return new RedissonSpringCacheManager(redissonClient, config);
  }
}
