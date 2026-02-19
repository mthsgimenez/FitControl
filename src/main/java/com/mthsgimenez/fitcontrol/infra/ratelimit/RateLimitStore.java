package com.mthsgimenez.fitcontrol.infra.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimitStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final String keyPrefix = "rl";

    public RateLimitStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long increaseRequestCount(String ipAddress) {
        String key = keyPrefix + ipAddress;
        return redisTemplate.opsForValue().increment(key);
    }

    public void setWindow(String ipAddress, Duration duration) {
        String key = keyPrefix + ipAddress;
        redisTemplate.expire(key, duration);
    }

    public Long getTTL(String ipAddress) {
        String key = keyPrefix + ipAddress;
        return redisTemplate.getExpire(key);
    }
}
