package com.iamfit.ai_service.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    // Un bucket por usuario en memoria
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String userId) {
        return buckets.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(String userId) {
        // 30 requests por hora por usuario (RN-8)
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofHours(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}