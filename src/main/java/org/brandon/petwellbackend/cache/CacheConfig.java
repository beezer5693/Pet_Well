package org.brandon.petwellbackend.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean(name = "tokenCache")
    public CacheStore<String, String> tokenCache() {
        return new CacheStore<>(900 * 4, TimeUnit.SECONDS);
    }
}
