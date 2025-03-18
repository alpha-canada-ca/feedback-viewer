package ca.gc.tbs.service;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
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
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("([a-zA-Z0-9_\\-\\.]+)\\s*@([\\sa-zA-Z0-9_\\-\\.]+)[\\.\\,]([a-zA-Z]{1,5})");

  // Singleton NLP pipeline for better performance
  private static final StanfordCoreNLP nlpPipeline;

  // Initialize the NLP pipeline once
  static {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    nlpPipeline = new StanfordCoreNLP(props);
  }

  public ContentService() {
    System.out.println("attempting to load bad words config...");
    BadWords.loadConfigs();
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

          // Skip if it's a common pronoun or if its POS tag is a pronoun (PRP or PRP$)
          if (!commonPronouns.contains(mentionText) && !pos.startsWith("PRP")) {
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
