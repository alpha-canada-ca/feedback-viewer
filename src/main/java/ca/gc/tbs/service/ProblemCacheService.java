package ca.gc.tbs.service;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProblemCacheService implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private ProblemRepository problemRepository;

    // This method will be triggered once the application is fully ready
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        getDistinctUrls(); // Populate the cache
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    @CacheEvict(value = "distinctUrls", allEntries = true)
    public void clearCacheDaily() {
        // This method will automatically clear the cached distinct URLs daily
        System.out.println("EVICTING CACHE");

    }

    // Method to refresh cache, can be called based on a scheduler or an event
    @CacheEvict(value = "distinctUrls", allEntries = true)
    public void invalidateDistinctUrlsCache() {
        System.out.println("EVICTING CACHE");
        // This method will force the next call to refreshDistinctUrls() to fetch fresh data
    }

    @Cacheable("distinctUrls")
    public List<Problem> getDistinctUrls() {
        System.out.println("Fetching distinct URLs from the database");
        List<Problem> x = problemRepository.findAllTest();
        System.out.println("done");
        return x;
    }
}
