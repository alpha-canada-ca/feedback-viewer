package ca.gc.tbs.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BadWords {
  private static final Logger logger = LoggerFactory.getLogger(BadWords.class);

  private static final Set<String> words = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private static final Set<String> allowedWords = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private static final String[] DEFAULT_FILES = {
    "static/badwords/badwords_en.txt", "static/badwords/badwords_fr.txt",
    //            "static/badwords/threats_fr.txt",
    //            "static/badwords/threats_en.txt"
  };
  private static final String ALLOWED_WORDS_FILE = "static/badwords/allowed_words.txt";

  public static void loadConfigs() {
    for (String file : DEFAULT_FILES) {
      loadFileConfigs(file);
    }
    loadAllowedWords(ALLOWED_WORDS_FILE);
    logger.info("Loaded {} words to filter out", words.size());
    logger.info("Loaded {} allowed words that will not be filtered", allowedWords.size());
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

  /**
   * Returns the set of allowed words that should not be redacted.
   * This is used by other services that need to know which words to exclude from redaction.
   */
  public static Set<String> getAllowedWords() {
    return Collections.unmodifiableSet(allowedWords);
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
