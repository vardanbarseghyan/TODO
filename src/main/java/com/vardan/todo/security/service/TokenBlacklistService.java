package com.vardan.todo.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";//This ensures Redis keys look like:blacklist:abc123     //this method receives token and stores it in Redis blacklist.
    public void blacklistToken(String token, long expirationMillis) {
        //this method receives token and stores it in Redis blacklist.
        redisTemplate.opsForValue().set(//opsForValue is a Redis operation for simple key-value storage
                BLACKLIST_PREFIX + token,//example if token is abc123, key becomes -> blacklist:abc123
                "true",//Value stored in Redis.Example->blacklist:abc123 → true, We don't really care about the value.
                expirationMillis,//We set expiration time.
                TimeUnit.MILLISECONDS//Redis automatically deletes key after 15(example) minutes.This prevents Redis memory from growing forever.
        );
    }
    public boolean isBlacklisted(String token) {
        //Check if Token is Blacklisted
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);//This method checks Redis.
    }
}
