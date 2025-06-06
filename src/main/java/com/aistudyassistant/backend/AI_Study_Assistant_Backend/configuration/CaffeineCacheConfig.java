package com.aistudyassistant.backend.AI_Study_Assistant_Backend.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    // this method is used to create a cache manager, which is used to create a cache with the name "users"
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("user");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    // this method is used to create a cache builder with the following properties
    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(500)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats();
    }
}
