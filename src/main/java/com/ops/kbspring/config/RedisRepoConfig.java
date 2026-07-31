package com.ops.kbspring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.ops.kbspring.repository.redis")
public class RedisRepoConfig {
}
