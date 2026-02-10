package ca.gc.tbs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Service
public class ContentService {
  private static final Logger logger = LoggerFactory.getLogger(ContentService.class);

  // Pre-compiled regex patterns for better performance
  private static final Pattern POSTAL_CODE_PATTERN =
      Pattern.compile("[A-Za-z]\\s*\\d\\s*[A-Za-z]\\s*[ -]?\\s*\\d\\s*[A-Za-z]\\s*\\d");
  private static final Pattern PASSPORT_PATTERN = Pattern.compile("\\b([A-Za-z]{2}\\s*\\d{6})\\b");
  private static final Pattern SIN_PATTERN =
      Pattern.compile("(\\d{3}\\s*\\d{3}\\s*\\d{3}|\\d{3}\\D*\\d{3}\\D*\\d{3})");
  private static final Pattern PHONE_PATTERN_1 =
      Pattern.compile("(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}");
  private static final Pattern PHONE_PATTERN_2 =
      Pattern.compile(
          "(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("([a-zA-Z0-9_+\\-\\.]+)\\s*@\\s*([a-zA-Z0-9_\\-\\.]+)(?:\\s*[\\.,]\\s*([a-zA-Z]{0,10}))?");

  // Address patterns for English and French street addresses
  private static final Pattern ADDRESS_PATTERN_1 =
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)" +
          "(?:\\s+(?:n|s|e|w|ne|nw|se|sw|o|no|so))?\\b");

  private static final Pattern ADDRESS_PATTERN_2 =
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+(?:n|s|e|w|ne|nw|se|sw|o|no|so)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)\\b");

  private static final Pattern ADDRESS_PATTERN_3 =
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)\\b");

  // Singleton NLP pipeline for better performance
  private static final StanfordCoreNLP nlpPipeline;

  static {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    nlpPipeline = new StanfordCoreNLP(props);
  }

  private final BadWords badWords;

  @Autowired
  public ContentService(BadWords badWords) {
    this.badWords = badWords;
  }

  private Set<String> getAllowedWords() {
    return badWords.getAllowedWords();
  }

  public String cleanContent(String content) {
    if (content.isEmpty()) {
      return content;
    }
    content = StringUtils.normalizeSpace(content);

    String newContent = badWords.censor(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Profanity filtered");
    }

    newContent = this.cleanPostalCode(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Postal code cleaned");
    }

    newContent = this.cleanPhoneNumber(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Phone number cleaned");
    }

    newContent = this.cleanPassportNumber(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Passport number cleaned");
    }

    newContent = this.cleanSIN(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("SIN cleaned");
    }

    newContent = this.cleanEmailAddress(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Email address cleaned");
    }

    newContent = this.cleanStreetAddress(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Street address cleaned");
    }

    newContent = this.cleanNames(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      logger.debug("Names cleaned");
    }

    return content;
  }

  private String cleanPostalCode(String content) {
    return POSTAL_CODE_PATTERN.matcher(content).replaceAll("### ###");
  }

  private String cleanPassportNumber(String content) {
    return PASSPORT_PATTERN.matcher(content).replaceAll("## ######");
  }

  private String cleanSIN(String content) {
    return SIN_PATTERN.matcher(content).replaceAll("### ### ###");
  }

  private String cleanPhoneNumber(String content) {
    content = PHONE_PATTERN_1.matcher(content).replaceAll("# ### ### ###");
    content = PHONE_PATTERN_2.matcher(content).replaceAll("# ### ### ###");
    return content;
  }

  private String cleanEmailAddress(String content) {
    return EMAIL_PATTERN.matcher(content).replaceAll("####@####.####");
  }

  private String cleanStreetAddress(String content) {
    content = ADDRESS_PATTERN_2.matcher(content).replaceAll("### #### ######");
    content = ADDRESS_PATTERN_1.matcher(content).replaceAll("### #### ######");
    content = ADDRESS_PATTERN_3.matcher(content).replaceAll("### #### ######");
    return content;
  }

  /**
   * Cleans person names from content using NLP entity recognition.
   */
  public String cleanNames(String content) {
    try {
      CoreDocument doc = new CoreDocument(content);
      nlpPipeline.annotate(doc);

      List<CoreEntityMention> entityMentions = new ArrayList<>(doc.entityMentions());
      Collections.reverse(entityMentions);

      StringBuilder sb = new StringBuilder(content);

      Set<String> commonPronouns =
          new HashSet<>(Arrays.asList("he", "she", "him", "her", "his", "hers"));

      for (CoreEntityMention em : entityMentions) {
        if (em.entityType().equals("PERSON")) {
          String mentionText = em.text().toLowerCase();
          String pos = em.tokens().get(0).tag();

          boolean isAllowed = getAllowedWords().contains(mentionText);

          if (!isAllowed && mentionText.contains(" ")) {
            for (String word : mentionText.split("\\s+")) {
              if (getAllowedWords().contains(word)) {
                isAllowed = true;
                break;
              }
            }
          }

          if (!commonPronouns.contains(mentionText) && !pos.startsWith("PRP") && !isAllowed) {
            int start = em.charOffsets().first();
            int end = em.charOffsets().second();
            char[] replacement = new char[end - start];
            Arrays.fill(replacement, '#');
            sb.replace(start, end, new String(replacement));
          }
        }
      }

      return sb.toString();
    } catch (Exception e) { 
        logger.error("Error during NLP processing", e);
      return content;
    }
  }
}
