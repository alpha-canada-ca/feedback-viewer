package ca.gc.tbs.config;

import ca.gc.tbs.service.ProblemCacheService;
import ca.gc.tbs.service.ProblemDateService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CachePreloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachePreloader.class);

    private final ProblemCacheService problemCacheService;
    private final ProblemDateService problemDateService;

    public CachePreloader(ProblemCacheService problemCacheService, ProblemDateService problemDateService) {
        this.problemCacheService = problemCacheService;
        this.problemDateService = problemDateService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void preloadCaches() {
        LOGGER.info("Starting cache preloading process...");
        LOGGER.info("Preloading distinct URLs cache...");
        problemCacheService.getDistinctProcessedUrlsForCache();
        LOGGER.info("Distinct URLs cache preloaded.");
        LOGGER.info("Preloading processed problems cache...");
        problemCacheService.getProcessedProblems();
        LOGGER.info("Processed problems cache preloaded.");
        LOGGER.info("Preloading problem dates cache...");
        problemDateService.getProblemDates();
        LOGGER.info("Problem dates cache preloaded.");
        LOGGER.info("All caches preloaded successfully.");
    }
}
