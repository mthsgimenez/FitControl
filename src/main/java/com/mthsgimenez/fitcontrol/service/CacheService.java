package com.mthsgimenez.fitcontrol.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplateObject;
    private final RedisTemplate<String, String> redisTemplateString;

    public CacheService(RedisTemplate<String, Object> redisTemplateObject, RedisTemplate<String, String> redisTemplateString) {
        this.redisTemplateObject = redisTemplateObject;
        this.redisTemplateString = redisTemplateString;
    }

    public void set(String key, Object value, Duration duration) {
        redisTemplateObject.opsForValue().set(key, value, duration);
    }

    public void set(String key, Object value) {
        redisTemplateObject.opsForValue().set(key, value);
    }

    public void set(String key, String value) {
        redisTemplateString.opsForValue().set(key, value);
    }

    public void set(String key, String value, Duration duration) {
        redisTemplateString.opsForValue().set(key, value, duration);
    }

    public Object getObject(String key) {
        return redisTemplateObject.opsForValue().get(key);
    }

    public String getString(String key) {
        return redisTemplateString.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplateObject.delete(key);
    }

    public Long increment(String key) {
        return redisTemplateObject.opsForValue().increment(key);
    }

    public void expire(String key, Duration duration) {
        redisTemplateObject.expire(key, duration);
    }

    public Long getTTL(String key) {
        return redisTemplateObject.getExpire(key);
    }
}
