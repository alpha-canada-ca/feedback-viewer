package ca.gc.tbs.service;
import ca.gc.tbs.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProblemDateService {

    @Autowired
    private ProblemRepository problemRepository;
    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    @CacheEvict(value = "problemDates", allEntries = true)
    public void clearCacheDaily() {
        // This method will automatically clear the cached problem dates daily
    }
    @Cacheable("problemDates")
    public Map<String, String> getProblemDates() {
        System.out.println("Fetching problem dates from the database");
        AggregationResults<Map> results = problemRepository.findEarliestAndLatestProblemDate();
        return results.getUniqueMappedResult();
    }

    // Method to refresh cache, can be called based on a scheduler or an event
    @CacheEvict(value = "problemDates", allEntries = true)
    public void refreshProblemDates() {
        // This method will force the next call to getProblemDates() to fetch fresh data
    }
}