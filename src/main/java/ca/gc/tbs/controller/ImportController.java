package ca.gc.tbs.controller;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import ca.gc.tbs.domain.Problem;
import ca.gc.tbs.domain.TopTaskSurvey;
import ca.gc.tbs.repository.ProblemRepository;
import ca.gc.tbs.repository.TopTaskRepository;
import ca.gc.tbs.service.ContentService;

@Controller
public class ImportController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportController.class);
  private static final int BATCH_SIZE = 1000;

  @Autowired
  ProblemRepository problemRepository;

  @Autowired
  TopTaskRepository topTaskRepository;

  @Autowired
  ContentService contentService;

  SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Import data from a local CSV file (feedback_export.csv) for performance testing.
   * Use ?limit=100 to test with fewer records first.
   */
  @GetMapping(value = "/importLocalCsv")
  public View importLocalCsv(@RequestParam(defaultValue = "100") int limit,
                              @RequestParam(defaultValue = "false") boolean clear) {
    String filePath = "feedback_export_fixed.csv";
    
    if (clear) {
      LOGGER.info("Clearing existing problems before import...");
      problemRepository.deleteAll();
    }
    
    long existingCount = problemRepository.count();
    LOGGER.info("Starting import from local CSV file: {} (limit: {}, existing records: {})", filePath, limit, existingCount);
    long startTime = System.currentTimeMillis();
    
    List<Problem> batch = new ArrayList<>(BATCH_SIZE);
    AtomicInteger count = new AtomicInteger(0);
    AtomicInteger errors = new AtomicInteger(0);
    
    try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1);
         CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      
      for (final CSVRecord record : parser) {
        // Check limit
        if (count.get() >= limit) {
          LOGGER.info("Reached limit of {} records, stopping import.", limit);
          break;
        }
        
        try {
          Problem problem = new Problem();
          problem.setId(UUID.randomUUID().toString());
          problem.setProblemDate(record.get("Problem Date"));
          problem.setTimeStamp(record.get("Time Stamp (UTC)"));
          problem.setProblemDetails(record.get("Problem Details"));
          problem.setLanguage(record.get("Language"));
          problem.setTitle(record.get("Title"));
          problem.setUrl(record.get("URL"));
          problem.setInstitution(record.get("Institution"));
          problem.setSection(record.get("Section"));
          problem.setTheme(record.get("Theme"));
          problem.setDeviceType(record.get("Device Type"));
          problem.setBrowser(record.get("Browser"));
          problem.setProcessed("true");
          problem.setAirTableSync("false");
          problem.setAutoTagProcessed("false");
          problem.setPersonalInfoProcessed("true");
          problem.setDataOrigin("MongoDB CSV Export");
          
          batch.add(problem);
          count.incrementAndGet();
          
          if (batch.size() >= BATCH_SIZE) {
            problemRepository.saveAll(batch);
            batch.clear();
            if (count.get() % 10000 == 0) {
              LOGGER.info("Imported {} records...", count.get());
            }
          }
        } catch (Exception e) {
          errors.incrementAndGet();
          if (errors.get() <= 10) {
            LOGGER.error("Error importing record: {}", e.getMessage());
          }
        }
      }
      
      // Save remaining batch
      if (!batch.isEmpty()) {
        problemRepository.saveAll(batch);
      }
      
    } catch (Exception e) {
      LOGGER.error("Error during CSV import: {}", e.getMessage());
    }
    
    long duration = System.currentTimeMillis() - startTime;
    LOGGER.info("Import complete! Imported {} records in {} ms ({} records/sec). Errors: {}", 
        count.get(), duration, (count.get() * 1000L / Math.max(1, duration)), errors.get());
    
    return new RedirectView("/pageFeedback");
  }

  @GetMapping(value = "/importcsv")
  public View importData() throws Exception {
    final Reader reader = new InputStreamReader(
        new URL(
            "https://docs.google.com/spreadsheets/d/1tTNrPJqKyNNkJo1UaCoSp1RMpSz3dJsRKmieDglSAOU/export?format=csv")
            .openConnection()
            .getInputStream(),
        StandardCharsets.UTF_8);
    final CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
    try {
      for (final CSVRecord record : parser) {
        try {
          Problem problem = new Problem();
          problem.setId(record.get("Ref Number").replace("/", ""));
          problem.setProblemDate(record.get("Date/time received"));
          problem.setTitle(record.get("Page Title"));
          problem.setUrl(record.get("Page URL"));
          problem.setProblemDetails(record.get("Details"));
          String[] topics = record.get("Topic").trim().split(",");
          if (topics.length > 0) {
            problem.setTags(Arrays.asList(topics));
          }
          problem.setInstitution("Health");
          if (problem.getUrl().contains("/en/")) {
            problem.setLanguage("en");
          } else {
            problem.setLanguage("fr");
          }
          problem.setProcessed("false");
          problem.setAirTableSync("false");
          problem.setAutoTagProcessed("false");
          problem.setPersonalInfoProcessed("false");
          problem.setDataOrigin("Health CSV");
          this.problemRepository.save(problem);

        } catch (Exception e) {
          System.out.println(e.getMessage());
          e.printStackTrace();
        }
      }

    } finally {
      parser.close();
      reader.close();
    }
    return new RedirectView("/pageFeedback");
  }

  /**
   * Import TopTaskSurvey data from a local CSV file.
   * Use ?limit=100 to test with fewer records first.
   * Use ?clear=true to delete existing records before import.
   */
  @GetMapping(value = "/importTopTaskCsv")
  public View importTopTaskCsv(@RequestParam(defaultValue = "100") int limit,
                               @RequestParam(defaultValue = "false") boolean clear) {
    String filePath = "top_task_survey_export_2025-12-22.csv";
    
    if (clear) {
      LOGGER.info("Clearing existing TopTaskSurvey records before import...");
      topTaskRepository.deleteAll();
    }
    
    long existingCount = topTaskRepository.count();
    LOGGER.info("Starting TopTaskSurvey import from local CSV file: {} (limit: {}, existing records: {})", filePath, limit, existingCount);
    long startTime = System.currentTimeMillis();
    
    List<TopTaskSurvey> batch = new ArrayList<>(BATCH_SIZE);
    AtomicInteger count = new AtomicInteger(0);
    AtomicInteger errors = new AtomicInteger(0);
    
    try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1);
         CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      
      for (final CSVRecord record : parser) {
        // Check limit
        if (count.get() >= limit) {
          LOGGER.info("Reached limit of {} records, stopping import.", limit);
          break;
        }
        
        try {
          TopTaskSurvey survey = new TopTaskSurvey();
          survey.setId(UUID.randomUUID().toString());
          survey.setDateTime(record.get("Date Time"));
          survey.setTimeStamp(record.get("Time Stamp (UTC)"));
          survey.setSurveyReferrer(record.get("Survey Referrer"));
          survey.setLanguage(record.get("Language"));
          survey.setDevice(record.get("Device"));
          survey.setScreener(record.get("Screener"));
          survey.setDept(record.get("Department"));
          survey.setTheme(record.get("Theme"));
          survey.setThemeOther(record.get("Theme Other"));
          survey.setGrouping(record.get("Grouping"));
          survey.setTask(record.get("Task"));
          survey.setTaskOther(record.get("Task Other"));
          survey.setTaskSatisfaction(record.get("Task Satisfaction"));
          survey.setTaskEase(record.get("Task Ease"));
          survey.setTaskCompletion(record.get("Task Completion"));
          survey.setTaskImprove(record.get("Task Improve"));
          survey.setTaskImproveComment(record.get("Task Improve Comment"));
          survey.setTaskWhyNot(record.get("Task Why Not"));
          survey.setTaskWhyNotComment(record.get("Task Why Not Comment"));
          survey.setTaskSampling(record.get("Task Sampling"));
          survey.setSamplingInvitation(record.get("Sampling Invitation"));
          survey.setSamplingGC(record.get("Sampling GC"));
          survey.setSamplingCanada(record.get("Sampling Canada"));
          survey.setSamplingTheme(record.get("Sampling Theme"));
          survey.setSamplingInstitution(record.get("Sampling Institution"));
          survey.setSamplingGrouping(record.get("Sampling Grouping"));
          survey.setSamplingTask(record.get("Sampling Task"));
          survey.setProcessed("true");
          survey.setTopTaskAirTableSync("false");
          survey.setAutoTagProcessed("false");
          survey.setPersonalInfoProcessed("true");
          
          batch.add(survey);
          count.incrementAndGet();
          
          if (batch.size() >= BATCH_SIZE) {
            topTaskRepository.saveAll(batch);
            batch.clear();
            if (count.get() % 10000 == 0) {
              LOGGER.info("Imported {} TopTaskSurvey records...", count.get());
            }
          }
        } catch (Exception e) {
          errors.incrementAndGet();
          if (errors.get() <= 10) {
            LOGGER.error("Error importing TopTaskSurvey record: {}", e.getMessage());
          }
        }
      }
      
      // Save remaining batch
      if (!batch.isEmpty()) {
        topTaskRepository.saveAll(batch);
      }
      
    } catch (Exception e) {
      LOGGER.error("Error during TopTaskSurvey CSV import: {}", e.getMessage());
    }
    
    long duration = System.currentTimeMillis() - startTime;
    LOGGER.info("TopTaskSurvey import complete! Imported {} records in {} ms ({} records/sec). Errors: {}", 
        count.get(), duration, (count.get() * 1000L / Math.max(1, duration)), errors.get());
    
    return new RedirectView("/topTaskSurvey");
  }
}
