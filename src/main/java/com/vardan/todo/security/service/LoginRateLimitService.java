package com.vardan.todo.security.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimitService {

    // Map stores bucket per IP address
    // key = user IP
    // value = bucket object
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();//We create one bucket per IP address.
    //1 IP address → 1 bucket
    //1 bucket → 5 tokens
    //1 token → 1 login attempt, chxarnes es token access tokeni het!!!!!

    // This method returns bucket for an IP address
    public Bucket resolveBucket(String ip) {

        // computeIfAbsent means:
        // if bucket already exists → return it
        // if not → create new bucket
        return buckets.computeIfAbsent(ip, this::newBucket);
    }


    // This method creates a new bucket
    private Bucket newBucket(String ip) {

        // limit = 5 requests
        // refill every 1 minute
        Bandwidth limit = Bandwidth.classic(
                5,                          // max tokens
                Refill.intervally(
                        5,                  // refill tokens
                        Duration.ofMinutes(3) // every 1 minute
                )//nenca config arac vor 1ropeum 5hatic avel pordz chi kara lini, pti spasen 1 rope eli bucketnery klcvi u eli kara pordzi login linel.
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
