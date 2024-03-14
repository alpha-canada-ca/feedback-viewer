package ca.gc.tbs.service;

import ca.gc.tbs.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProblemCacheService {

    @Autowired
    private ProblemRepository problemRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    @CacheEvict(value = "distinctUrls", allEntries = true)
    public void clearCacheDaily() {
        // This method will automatically clear the cached distinct URLs daily
    }

    @Cacheable("distinctUrls")
    public AggregationResults<Map> refreshDistinctUrls() {
        System.out.println("Fetching distinct URLs from the database");
        return problemRepository.findDistinctUrlsWithDetails();
    }

    // Method to refresh cache, can be called based on a scheduler or an event
    @CacheEvict(value = "distinctUrls", allEntries = true)
    public void invalidateDistinctUrlsCache() {
        // This method will force the next call to findDistinctUrlsWithDetails() to fetch fresh data
    }
}