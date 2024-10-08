package com.example.HttpDownloadServer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisToolConfig {
    @Autowired
    private RedisTemplate redisTemplate;
    @Bean
    public RedisTemplate redisTemplateInit() {
        // Tool for setting serialization keys
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // A tool for setting serialized values
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // Set the hash key
        redisTemplate.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
        // Set the hash value
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
