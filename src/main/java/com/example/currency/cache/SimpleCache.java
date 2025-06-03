package com.example.currency.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SimpleCache {
    private final Map<String, Object> cache = new HashMap<>();

    public void put(String key, Object value) {
        System.out.println("Cache put: key=" + key + ", value=" + value);
        cache.put(key, value);
    }

    public Optional<Object> get(String key) {
        System.out.println("Cache get: key=" + key);
        Optional<Object> result = Optional.ofNullable(cache.get(key));
        if (result.isPresent()) {
            System.out.println("Cache hit: key=" + key + ", value=" + result.get());
        } else {
            System.out.println("Cache miss: key=" + key);
        }
        return result;
    }

    public void remove(String key) {
        System.out.println("Cache remove: key=" + key);
        cache.remove(key);
    }

    public void clear() {
        System.out.println("Cache clear: all entries removed");
        cache.clear();
    }
}