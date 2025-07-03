package ca.gc.tbs.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("problemDates", "distinctUrls", "processedProblems");
  }
}
