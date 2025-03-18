package ca.gc.tbs.service;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.repository.ProblemRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProblemCacheService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProblemCacheService.class);
  @Autowired private ProblemRepository problemRepository;

  @Scheduled(cron = "0 0 0 * * *")
  @CacheEvict(value = "distinctUrls", allEntries = true)
  public void clearCacheDaily() {
    // This method will automatically clear the cached distinct URLs daily
    System.out.println("EVICTING CACHE");
  }

  @Cacheable("distinctUrls")
  public List<Problem> getProcessedProblems() {
    return problemRepository.findAllProcessedProblems();
  }
}
