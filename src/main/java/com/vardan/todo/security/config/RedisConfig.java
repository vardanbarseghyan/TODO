package com.vardan.todo.security.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching//enables Spring caching features
public class RedisConfig {
    @Bean
    //RedisConnectionFactory object responsible for connecting to Redis
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();//Redis driver used by Spring Boot
    }//Spring → connect to Redis server
    @Bean
    //RedisTemplate -> main tool used to interact with Redis
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);//tell RedisTemplate which Redis connection to use
        return template;
    }

}
