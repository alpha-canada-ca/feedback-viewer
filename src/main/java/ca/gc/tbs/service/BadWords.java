package ca.gc.tbs.service;

import ca.gc.tbs.domain.BadWordEntry;
import ca.gc.tbs.repository.BadWordEntryRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing bad words, profanity filtering, threat detection, and allowed words.
 * Loads word lists from MongoDB into in-memory caches for fast lookup.
 * Thread-safe using ConcurrentHashMap.
 */
@Service
public class BadWords {
  private static final Logger logger = LoggerFactory.getLogger(BadWords.class);

  private final BadWordEntryRepository badWordEntryRepository;
  
  // In-memory caches for fast word lookup
  private final Set<String> profanityWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<String> threatWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<String> allowedWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<String> errorKeywords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  
  // Combined set of all words to filter (profanity + threats)
  private final Set<String> allFilterWords = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private Pattern filterPattern;

  @Autowired
  public BadWords(BadWordEntryRepository badWordEntryRepository) {
    this.badWordEntryRepository = badWordEntryRepository;
  }

  /**
   * Loads word configurations from MongoDB on service initialization.
   * Called automatically by Spring after dependency injection.
   */
  @PostConstruct
  public void loadConfigs() {
    logger.info("=== BadWords.loadConfigs() START ===");
    logger.info("Loading word configurations from MongoDB...");
    
    try {
      // Load profanity words
      logger.info("Querying profanity words from MongoDB...");
      List<BadWordEntry> profanityEntries = badWordEntryRepository.findByTypeAndActive("profanity", true);
      logger.info("Found {} profanity entries", profanityEntries.size());
      profanityEntries.forEach(entry -> {
        String word = entry.getWord().trim().toLowerCase();
        profanityWords.add(word);
        allFilterWords.add(word);
        compileFilterPattern();
      });
      
      // Load threat words
      logger.info("Querying threat words from MongoDB...");
      List<BadWordEntry> threatEntries = badWordEntryRepository.findByTypeAndActive("threat", true);
      logger.info("Found {} threat entries", threatEntries.size());
      threatEntries.forEach(entry -> {
        String word = entry.getWord().trim().toLowerCase();
        threatWords.add(word);
        allFilterWords.add(word);
      });
      
      // Load allowed words
      logger.info("Querying allowed words from MongoDB...");
      List<BadWordEntry> allowedEntries = badWordEntryRepository.findByTypeAndActive("allowed", true);
      logger.info("Found {} allowed entries", allowedEntries.size());
      allowedEntries.forEach(entry -> {
        String word = entry.getWord().trim().toLowerCase();
        allowedWords.add(word);
      });
      
      // Load error keywords
      logger.info("Querying error keywords from MongoDB...");
      List<BadWordEntry> errorEntries = badWordEntryRepository.findByTypeAndActive("error", true);
      logger.info("Found {} error entries", errorEntries.size());
      errorEntries.forEach(entry -> {
        String word = entry.getWord().trim().toLowerCase();
        errorKeywords.add(word);
      });
      
      logger.info("Loaded {} profanity words", profanityWords.size());
      logger.info("Loaded {} threat words", threatWords.size());
      logger.info("Loaded {} words to filter out (total)", allFilterWords.size());
      logger.info("Loaded {} allowed words that will not be filtered", allowedWords.size());
      logger.info("Loaded {} error keywords", errorKeywords.size());
      logger.info("=== BadWords.loadConfigs() COMPLETED SUCCESSFULLY ===");
      
    } catch (Exception e) {
      logger.error("=== BadWords.loadConfigs() FAILED ===");
      logger.error("Failed to load word configurations from MongoDB. Service will start with empty word lists.", e);
      logger.error("Exception type: {}", e.getClass().getName());
      logger.error("Exception message: {}", e.getMessage());
      logger.warn("Please ensure MongoDB is running and the 'badwords' collection is populated.");
      // Re-throw to prevent silent failures during development
      throw new RuntimeException("Failed to initialize BadWords service", e);
    }
  }

  /**
   * Returns the set of allowed words that should not be redacted.
   * This is used by other services that need to know which words to exclude from redaction.
   * 
   * @return Unmodifiable set of allowed words
   */
  public Set<String> getAllowedWords() {
    return Collections.unmodifiableSet(allowedWords);
  }
  
  /**
   * Returns the set of error keywords.
   * 
   * @return Unmodifiable set of error keywords
   */
  public Set<String> getErrorKeywords() {
    return Collections.unmodifiableSet(errorKeywords);
  }
  
  /**
   * Returns the set of profanity words.
   * 
   * @return Unmodifiable set of profanity words
   */
  public Set<String> getProfanityWords() {
    return Collections.unmodifiableSet(profanityWords);
  }
  
  /**
   * Returns the set of threat words.
   * 
   * @return Unmodifiable set of threat words
   */
  public Set<String> getThreatWords() {
    return Collections.unmodifiableSet(threatWords);
  }


  private void compileFilterPattern() {
    if (allFilterWords.isEmpty()) {
      filterPattern = null;
      return;
    }
    String patternString = allFilterWords.stream()
            .filter(word -> word != null && !word.trim().isEmpty())
            .map(Pattern::quote)
            .map(word -> "\\b" + word + "\\b")  // exact whole word only
            .collect(Collectors.joining("|"));
    filterPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  }

  /**
   * Censors profanity and threats in the given text by replacing them with asterisks.
   * Words in the allowed words list are never censored.
   *
   * @param text The text to censor
   * @return The censored text
   */
  public String censor(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }
    if (filterPattern == null) {
      // No filter words loaded
      return text;
    }

    Matcher matcher = filterPattern.matcher(text);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String match = matcher.group();
      String normalized = match.toLowerCase().replaceAll("[^a-zà-ÿ]", "");

      if (allowedWords.contains(normalized)) {
        matcher.appendReplacement(result, Matcher.quoteReplacement(match));
      } else {
        matcher.appendReplacement(result, Matcher.quoteReplacement(createMask(match)));
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Creates a mask of asterisks for a given word.
   * 
   * @param word The word to mask
   * @return A string of asterisks with the same length as the word
   */
  private String createMask(String word) {
    return word.replaceAll(".", "*");
  }
  
  /**
   * Reloads word configurations from MongoDB.
   * Useful for refreshing the cache without restarting the application.
   */
  public void reload() {
    logger.info("Reloading word configurations from MongoDB...");
    
    // Clear existing caches
    profanityWords.clear();
    threatWords.clear();
    allowedWords.clear();
    errorKeywords.clear();
    allFilterWords.clear();
    compileFilterPattern();
    
    // Reload from database
    loadConfigs();
  }
}
