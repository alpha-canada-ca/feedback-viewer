package ca.gc.tbs.service;

import ca.gc.tbs.repository.ProblemRepository;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProblemDateService {

  private static final Logger logger = LoggerFactory.getLogger(ProblemDateService.class);

  @Autowired private ProblemRepository problemRepository;

  @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight UTC
  @CacheEvict(value = "problemDates", allEntries = true)
  public void clearCacheDaily() {
    logger.info("Clearing problemDates cache at {}", ZonedDateTime.now(ZoneOffset.UTC));
  }

  @Cacheable(value = "problemDates", key = "'all'", unless = "#result == null")
  public Map<String, String> getProblemDates() {
    logger.info(
        "Fetching problem dates from the database at {}", ZonedDateTime.now(ZoneOffset.UTC));
    AggregationResults<Map> results = problemRepository.findEarliestAndLatestProblemDate();
    Map<String, String> resultMap = results.getUniqueMappedResult();
    logger.info("Fetched problem dates: {}", resultMap);
    return resultMap;
  }

  // Method to refresh cache, can be called based on a scheduler or an event
  @CacheEvict(value = "problemDates", allEntries = true)
  public void refreshProblemDates() {
    logger.info("Manually refreshing problemDates cache at {}", ZonedDateTime.now(ZoneOffset.UTC));
  }
}
