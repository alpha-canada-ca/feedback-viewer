package ca.gc.tbs.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ca.gc.tbs.domain.BadWordEntry;
import ca.gc.tbs.repository.BadWordEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BadWords {
  private static final Logger logger = LoggerFactory.getLogger(BadWords.class);

  private static final Set<String> words = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private static final Set<String> allowedWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private static final Set<String> threats = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private static final Set<String> errorKeywords = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final String[] DEFAULT_FILES = {
          "static/badwords/badwords_en.txt",
          "static/badwords/badwords_fr.txt"
    };

  private static BadWordEntryRepository repository;

    public static void setRepository(BadWordEntryRepository repo) {
        repository = repo;
    }

    private static final String ALLOWED_WORDS_FILE = "static/wordlists/allowed_words.txt";

  public static void loadConfigs() {
    if (repository == null) {
        logger.warn("No repository - loading from files only");
        for (String file : DEFAULT_FILES) {
            loadFileConfigs(file);
        }
        loadAllowedWords(ALLOWED_WORDS_FILE);
        logger.info("Loaded {} words to filter out", words.size());
        logger.info("Loaded {} allowed words that will not be filtered", allowedWords.size());
        return;
    }
      // Check if database is empty (check all types)
      long profanityCount = repository.countByType("profanity");
      long threatCount = repository.countByType("threat");
      long allowedCount = repository.countByType("allowed");
      long errorCount = repository. countByType("error");

      logger.info("Current DB counts - profanity: {}, threats: {}, allowed:  {}, errors: {}",
              profanityCount, threatCount, allowedCount, errorCount);

    //migrate if any type is empty
      if (needsMigration()) {
          logger.info("Database missing data, migrating now");
          migrateToDatabase();
      }

      //load badwords from database
      loadAllFromDatabase();

      logger.info("Loaded {} profanity, {} threats, {} allowed, {} errors",
              words.size(), threats.size(), allowedWords.size(), errorKeywords.size());
  }

    private static boolean needsMigration() {
        return repository.countByType("profanity") == 0 ||
                repository.countByType("threat") == 0 ||
                repository.countByType("allowed") == 0 ||
                repository.countByType("error") == 0;
    }

    private static void loadAllFromDatabase() {
        loadFromDatabase("profanity", words);
        loadFromDatabase("threat", threats);
        loadFromDatabase("allowed", allowedWords);
        loadFromDatabase("error", errorKeywords);
    }

  private static void loadFromDatabase(String type, Set<String> targetSet) {
      targetSet.clear();
      repository.findByTypeAndActive(type,true)
              .forEach(entry -> targetSet.add(entry. getWord().toLowerCase()));
      logger.info("Loaded {} {} words from database", targetSet.size(), type);
  }

  private static void migrateToDatabase() {
      List<BadWordEntry> entries = new ArrayList<>();

      //badwords
      if (repository.countByType("profanity") == 0) {
          logger.info("Migrating profanity words.. .");
          entries.addAll(loadFileForMigration("static/badwords/badwords_en.txt", "en", "profanity"));
          entries.addAll(loadFileForMigration("static/badwords/badwords_fr.txt", "fr", "profanity"));
      }

      //threats
      if (repository.countByType("threat") == 0) {
          logger.info("Migrating threat words...");
          entries.addAll(loadFileForMigration("static/wordlists/threats_en.txt", "en", "threat"));
          entries.addAll(loadFileForMigration("static/wordlists/threats_fr.txt", "fr", "threat"));
      }

      //allowed words
      if (repository.countByType("allowed") == 0) {
          logger.info("Migrating allowed words...");
          entries.addAll(loadFileForMigration("static/wordlists/allowed_words.txt", "en", "allowed"));
      }

      //error keywords
      if (repository.countByType("error") == 0) {
          logger.info("Migrating error keywords...");
          entries.addAll(loadFileForMigration("static/error_keywords/errors_en.txt", "en", "error"));
          entries.addAll(loadFileForMigration("static/error_keywords/errors_fr.txt", "fr", "error"));
          entries.addAll(loadFileForMigration("static/error_keywords/errors_bilingual.txt", "bilingual", "error"));
      }

      if (!entries.isEmpty()) {
          repository. saveAll(entries);
          logger.info("MIGRATED {} words to database", entries.size());
      } else {
          logger.warn("Nothing to migrate!");
      }
  }

    private static List<BadWordEntry> loadFileForMigration(String filePath, String language, String type) {
        List<BadWordEntry> entries = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try {
            Resource resource = new ClassPathResource(filePath, BadWords.class. getClassLoader());
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                reader.lines()
                        .map(String::trim)
                        .filter(line -> ! line.isEmpty())
                        .forEach(line -> {
                            String w = line.toLowerCase();
                            if (! seen.contains(w)) {
                                seen.add(w);
                                entries.add(new BadWordEntry(w, language, type));
                            }
                        });
            }
            logger.info("Read {} words from {} for migration", entries.size(), filePath);
        } catch (Exception e) {
            logger.warn("Could not load {}: {}", filePath, e.getMessage());
        }
        return entries;
    }
  
  private static void loadAllowedWords(String filePath) {
    try {
      Resource resource = new ClassPathResource(filePath, BadWords.class.getClassLoader());
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
        allowedWords.addAll(
            reader.lines().map(String::trim).map(String::toLowerCase).collect(Collectors.toSet()));
      }
    } catch (Exception e) {
      logger.warn("Allowed words file {} not found, creating empty set", filePath);
      // If file doesn't exist yet, start with an empty set but add CARM as default
      allowedWords.add("carm");
    }
  }

  private static void loadFileConfigs(String filePath) {
    try {
      Resource resource = new ClassPathResource(filePath, BadWords.class.getClassLoader());
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
        words.addAll(
            reader.lines().map(String::trim).map(String::toLowerCase).collect(Collectors.toSet()));
      }
    } catch (Exception e) {
      logger.error("Error loading file config {}", filePath, e);
    }
  }

  private static void loadGoogleConfigs(String googleSheetUrl) {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new URL(googleSheetUrl).openConnection().getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        words.add(line.trim().split(",")[0].toLowerCase()); // assuming first column has the word
      }
    } catch (IOException e) {
      logger.error("Error loading Google config from {}", googleSheetUrl, e);
    }
  }


  /**
   * Returns the set of allowed words that should not be redacted.
   * This is used by other services that need to know which words to exclude from redaction.
   */
  public static Set<String> getAllowedWords() {
    return Collections.unmodifiableSet(allowedWords);
  }

  public static Set<String> getWords() {
      return Collections.unmodifiableSet(words);
  }

  public static void refresh() {
      if (repository != null) {
          loadAllFromDatabase();
          logger.info("All words cache refreshed");
      }
  }

  public static String censor(String text) {
    StringBuilder result = new StringBuilder();
    for (String word : text.split("\\s+")) {
      String wordToCheck =
          word.toLowerCase()
              .replaceAll("[^a-zà-ÿ]", ""); // Including accented characters for French
      
      // Skip censoring if the word is in the allowed words list
      boolean shouldCensor = words.stream().anyMatch(wordToCheck::contains) &&
                             !allowedWords.contains(wordToCheck);
      
      result
          .append(shouldCensor ? createMask(word) : word)
          .append(' ');
    }
    return result.toString().trim();
  }

  private static String createMask(String word) {
    return word.replaceAll(".", "*");
  }
}
