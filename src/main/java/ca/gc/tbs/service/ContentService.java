package ca.gc.tbs.service;

import ca.gc.tbs.repository.BadWordEntryRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

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
  // Updated email pattern to catch obfuscated emails: supports +, spaces in domain, optional TLD, longer TLDs
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("([a-zA-Z0-9_+\\-\\.]+)\\s*@\\s*([a-zA-Z0-9_\\-\\.]+)(?:\\s*[\\.,]\\s*([a-zA-Z]{0,10}))?");
  
  // Address patterns for English and French street addresses
  // Pattern 1: NUMBER + WORD(S) + SUFFIX + DIRECTION (optional)
  private static final Pattern ADDRESS_PATTERN_1 = 
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)" +
          "(?:\\s+(?:n|s|e|w|ne|nw|se|sw|o|no|so))?\\b");
  
  // Pattern 2: NUMBER + DIRECTION + WORD(S) + SUFFIX
  private static final Pattern ADDRESS_PATTERN_2 = 
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+(?:n|s|e|w|ne|nw|se|sw|o|no|so)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)\\b");
  
  // Pattern 3: NUMBER + WORD(S) + SUFFIX (fallback pattern)
  private static final Pattern ADDRESS_PATTERN_3 = 
      Pattern.compile("(?i)\\b(\\d{1,6}[A-Za-z]?)\\s+([A-Za-z][A-Za-z''\\-]*(?:\\s+[A-Za-z][A-Za-z''\\-]*){0,3})\\s+" +
          "(?:st|street|ave|avenue|av|av\\.|rd|road|dr|drive|blvd|boulevard|boul|boul\\.|ln|lane|ct|court|pl|place|ter|terrace|terr|pkwy|parkway|cir|circle|hwy|highway|rue|chemin|ch|ch\\.|chem|chem\\.|route|rte|all[ée]e?|all\\.|allee|cours|voie|terrain|terrasse|rang|promenade|prom|prom\\.)\\b");

  // Singleton NLP pipeline for better performance
  private static final StanfordCoreNLP nlpPipeline;

  // Initialize the NLP pipeline once
  static {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    nlpPipeline = new StanfordCoreNLP(props);
  }

  // Set of allowed words that should never be redacted, loaded from BadWords
  private Set<String> allowedWords;
  private final BadWordEntryRepository badWordEntryRepository;

  @Autowired
  public ContentService(BadWordEntryRepository badWordEntryRepository) {
      this.badWordEntryRepository = badWordEntryRepository;

    System.out.println("attempting to load bad words config...");
    BadWords.setRepository(badWordEntryRepository);
    BadWords.loadConfigs();
    // Get the allowed words from BadWords class
    this.allowedWords = BadWords.getAllowedWords();
  }

  public String cleanContent(String content) {
    if (content.isEmpty()) {
      return content; // Return empty string if content is empty
    }
    content = StringUtils.normalizeSpace(content);
    String newContent = BadWords.censor(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("curse words cleaned: " + content);
    }
    newContent = this.cleanPostalCode(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Postal code cleaned: " + content);
    }
    newContent = this.cleanPhoneNumber(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Phone number cleaned: " + content);
    }
    newContent = this.cleanPassportNumber(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Passport number cleaned: " + content);
    }
    newContent = this.cleanSIN(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("SIN number cleaned: " + content);
    }
    newContent = this.cleanEmailAddress(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Email Address cleaned: " + content);
    }
    newContent = this.cleanStreetAddress(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Street address cleaned: " + content);
    }
    newContent = this.cleanNames(content);
    if (!newContent.contentEquals(content)) {
      content = newContent;
      System.out.println("Names cleaned: " + content);
    }
    return content;
  }

  /** Cleans postal codes from the content. */
  private String cleanPostalCode(String content) {
    return POSTAL_CODE_PATTERN.matcher(content).replaceAll("### ###");
  }

  /** Cleans passport numbers from the content. */
  private String cleanPassportNumber(String content) {
    return PASSPORT_PATTERN.matcher(content).replaceAll("## ######");
  }

  /** Cleans SIN numbers from the content. */
  private String cleanSIN(String content) {
    return SIN_PATTERN.matcher(content).replaceAll("### ### ###");
  }

  /** Cleans phone numbers from the content. */
  private String cleanPhoneNumber(String content) {
    content = PHONE_PATTERN_1.matcher(content).replaceAll("# ### ### ###");
    content = PHONE_PATTERN_2.matcher(content).replaceAll("# ### ### ###");
    return content;
  }

  /** Cleans email addresses from the content. */
  private String cleanEmailAddress(String content) {
    return EMAIL_PATTERN.matcher(content).replaceAll("####@####.####");
  }

  /** 
   * Cleans street addresses from the content.
   * Applies three patterns in order: 
   * 1. Pattern 2 (NUMBER + DIRECTION + WORD(S) + SUFFIX) - most specific
   * 2. Pattern 1 (NUMBER + WORD(S) + SUFFIX + optional DIRECTION)
   * 3. Pattern 3 (NUMBER + WORD(S) + SUFFIX) - fallback
   */
  private String cleanStreetAddress(String content) {
    // Apply Pattern 2 first (most specific with direction)
    content = ADDRESS_PATTERN_2.matcher(content).replaceAll("### #### ######");
    // Apply Pattern 1 (with optional direction at end)
    content = ADDRESS_PATTERN_1.matcher(content).replaceAll("### #### ######");
    // Apply Pattern 3 last (fallback pattern)
    content = ADDRESS_PATTERN_3.matcher(content).replaceAll("### #### ######");
    return content;
  }

  /**
   * Cleans the names of persons in the provided content by replacing them with '#' characters. Uses
   * StanfordCoreNLP for natural language processing and entity recognition. Reverses the list of
   * entity mentions to replace them from the end of the string, preserving the indices of earlier
   * mentions when replacing later ones.
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

          // Skip if it's a common pronoun, if its POS tag is a pronoun (PRP or PRP$),
          // or if it's in our allowed words list
          boolean isAllowed = allowedWords.contains(mentionText);
          
          // Also check if any word in the mention is in the allowed list
          // This handles multi-word names where one part might be allowed
          if (!isAllowed && mentionText.contains(" ")) {
            for (String word : mentionText.split("\\s+")) {
              if (allowedWords.contains(word)) {
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
      System.out.println("Error during NLP processing: " + e.getMessage());
      // Return original content if NLP processing fails
      return content;
    }
  }
}
