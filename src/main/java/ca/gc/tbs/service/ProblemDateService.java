package ca.gc.tbs.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProblemDateService {

  private static final Logger logger = LoggerFactory.getLogger(ProblemDateService.class);

  @Cacheable(value = "problemDates", key = "'all'", unless = "#result == null")
  public Map<String, String> getProblemDates() {
    logger.info("Calculating problem dates based on fiscal quarters (cache miss or initial load).");
    LocalDate currentDate = LocalDate.now();
    LocalDate earliestDate;
    LocalDate latestDate;

    int currentMonth = currentDate.getMonthValue();
    int currentYear = currentDate.getYear();

    // Determine the current fiscal quarter and calculate the date range
    if (currentMonth >= Month.APRIL.getValue() && currentMonth <= Month.JUNE.getValue()) {
      // Q1 (April 1 - June 30) - Show Q4 (previous year) and Q1 (current year)
      earliestDate = LocalDate.of(currentYear - 1, Month.OCTOBER, 1);
      latestDate = LocalDate.of(currentYear, Month.JUNE, 30);
    } else if (currentMonth >= Month.JULY.getValue() && currentMonth <= Month.SEPTEMBER.getValue()) {
      // Q2 (July 1 - September 30) - Show Q1 and Q2
      earliestDate = LocalDate.of(currentYear, Month.APRIL, 1);
      latestDate = LocalDate.of(currentYear, Month.SEPTEMBER, 30);
    } else if (currentMonth >= Month.OCTOBER.getValue() && currentMonth <= Month.DECEMBER.getValue()) {
      // Q3 (October 1 - December 31) - Show Q2 and Q3
      earliestDate = LocalDate.of(currentYear, Month.JULY, 1);
      latestDate = LocalDate.of(currentYear, Month.DECEMBER, 31);
    } else {
      // Q4 (January 1 - March 31) - Show Q3 (previous year) and Q4 (current year)
      earliestDate = LocalDate.of(currentYear - 1, Month.JULY, 1);
      latestDate = LocalDate.of(currentYear, Month.MARCH, 31);
    }

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("earliestDate", earliestDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    resultMap.put("latestDate", latestDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

    logger.info("Calculated problem dates: {}", resultMap);
    return resultMap;
  }

  // The clearCacheDaily and refreshProblemDates methods are no longer needed as dates are calculated
  // @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight UTC
  // @CacheEvict(value = "problemDates", allEntries = true)
  // public void clearCacheDaily() {
  //   logger.info("Clearing problemDates cache at {}", ZonedDateTime.now(ZoneOffset.UTC));
  // }

  // @CacheEvict(value = "problemDates", allEntries = true)
  // public void refreshProblemDates() {
  //   logger.info("Manually refreshing problemDates cache at {}", ZonedDateTime.now(ZoneOffset.UTC));
  // }
}
