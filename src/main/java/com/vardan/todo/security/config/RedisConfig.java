package com.vardan.todo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

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
        template.setKeySerializer(new StringRedisSerializer());//ays 2 toxery  harkavor en vorpisi cache-ery pahven redis-um JSON,ete sranq chlinen
        //defaultov kpahven` Java's default serializer, which stores data as raw binary bytes.Inchy kdjvarecni debig anely.
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
    /**
     * Redis Configuration
     * ====================
     *
     * This class configures Redis for TWO purposes:
     *
     * 1. EXISTING: Token blacklisting (using RedisTemplate directly)
     *    → Your TokenBlacklistService stores "blacklist:tokenXYZ" = "true" in Redis
     *    → This is low-level, manual Redis usage
     *
     * 2. NEW: Performance caching (using Spring's @Cacheable annotations)
     *    → Automatically cache method results in Redis
     *    → Spring handles storing and retrieving — you just add annotations
     *
     * HOW SPRING CACHING WORKS:
     * =========================
     * When you put @Cacheable("categories") on a method:
     *   - First call  → method runs normally → result stored in Redis → returned to caller
     *   - Second call → Spring checks Redis → finds the cached result → returns it WITHOUT running the method
     *   - The method body is completely SKIPPED on cache hits
     *
     * When you put @CacheEvict("categories") on a method:
     *   - The cached data is DELETED from Redis
     *   - Next @Cacheable call will hit the database again and re-cache
     *
     * WHY JSON SERIALIZATION?
     * =======================
     * By default, Spring stores cached objects as Java-serialized bytes in Redis.
     * This is problematic because:
     *   - You can't read the data in Redis CLI (it's binary gibberish)
     *   - If you change your DTO class (add/remove fields), old cached data
     *     becomes unreadable and throws deserialization errors
     *
     * With JSON serialization, cached data is stored as readable JSON strings,
     * which is more robust and debuggable.
     */
    // NEW: Cache Manager — controls how @Cacheable stores data in Redis
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Handles LocalDateTime serialization

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
