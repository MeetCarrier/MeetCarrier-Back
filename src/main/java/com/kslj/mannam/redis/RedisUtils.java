package com.kslj.mannam.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setData(String key, Object value, long expiredTime) {
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.SECONDS);
    }

    public Optional<Object> getData(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public Long incrData(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}