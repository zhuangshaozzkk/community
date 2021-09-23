package com.zzkk.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.thymeleaf.TemplateEngine;

/**
 * @author zzkk
 * @ClassName RedisConfig
 * @Description Todo
 **/
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        // 设置key的序列化方式
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        redisTemplate.setValueSerializer(RedisSerializer.json());
        // hash
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        // 始化参数和初始化工作
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
