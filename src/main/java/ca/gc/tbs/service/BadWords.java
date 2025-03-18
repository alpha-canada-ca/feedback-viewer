package ca.gc.tbs.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
  private static final String[] DEFAULT_FILES = {
    "static/badwords/badwords_en.txt", "static/badwords/badwords_fr.txt",
    //            "static/badwords/threats_fr.txt",
    //            "static/badwords/threats_en.txt"
  };

  public static void loadConfigs() {
    for (String file : DEFAULT_FILES) {
      loadFileConfigs(file);
    }
    loadGoogleConfigs(
        "https://docs.google.com/spreadsheets/d/1hIEi2YG3ydav1E06Bzf2mQbGZ12kh2fe4ISgLg_UBuM/export?format=csv");
    logger.info("Loaded {} words to filter out", words.size());
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

  public static String censor(String text) {
    StringBuilder result = new StringBuilder();
    for (String word : text.split("\\s+")) {
      String wordToCheck =
          word.toLowerCase()
              .replaceAll("[^a-zà-ÿ]", ""); // Including accented characters for French
      result
          .append(words.stream().anyMatch(wordToCheck::contains) ? createMask(word) : word)
          .append(' ');
    }
    return result.toString().trim();
  }

  private static String createMask(String word) {
    return word.replaceAll(".", "*");
  }
}
