package ca.gc.tbs.service;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContentService {

    public ContentService() {
        System.out.println("attempting to load bad words config...");
        BadWords.loadConfigs();
    }

    public String cleanContent(String content) {
        if (content.isEmpty()) {
            return content; // Return empty string if content is empty
        }
        content = StringUtils.normalizeSpace(content);
//        String newContent = BadWords.censor(content);
//        if (!newContent.contentEquals(content) && newContent.contains("#")) {
//            content = newContent;
//            System.out.println("curse words cleaned: " + content);
//        }
        String newContent = this.cleanPostalCode(content);
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

    // [A-Za-z] matches any uppercase or lowercase letter
    // \s* matches any whitespace character (space or tab) zero or more times
    // \d matches any single digit
    // \s*[A-Za-z]\s* matches any uppercase or lowercase letter, zero or more
    // whitespace characters around it
    // [ -]?\s* matches an optional space or dash, zero or more whitespace
    // characters around it
    // \d\s* matches any single digit, zero or more whitespace characters around it
    // [A-Za-z]\s*\d matches any uppercase or lowercase letter, zero or more
    // whitespace characters around it and any single digit
    private String cleanPostalCode(String content) {
        return content.replaceAll("[A-Za-z]\\s*\\d\\s*[A-Za-z]\\s*[ -]?\\s*\\d\\s*[A-Za-z]\\s*\\d", "### ###");
    }

    // b matches a word boundary
    // ([A-Za-z]{2} matches 2 characters of any uppercase or lowercase letters
    // s* matches any whitespace character (space or tab) zero or more times
    // d{6} matches 6 digits
    // b matches a word boundary
    private String cleanPassportNumber(String content) {
        return content.replaceAll("\\b([A-Za-z]{2}\\s*\\d{6})\\b", "## ######");
    }

    // (\d{3}\s*\d{3}\s*\d{3}|\d{3}\D*\d{3}\D*\d{3}) the regular expression is using
    // the | operator which means "or" and this is used to specify two different
    // patterns to match.
    // The first pattern \d{3}\s*\d{3}\s*\d{3} matches 3 digits, zero or more
    // whitespace characters, 3 digits, zero or more whitespace characters, and 3
    // digits.
    // The second pattern \d{3}\D*\d{3}\D*\d{3} matches 3 digits, zero or more
    // non-digit characters, 3 digits, zero or more non-digit characters, and 3
    // digits.
    private String cleanSIN(String content) {
        return content.replaceAll("(\\d{3}\\s*\\d{3}\\s*\\d{3}|\\d{3}\\D*\\d{3}\\D*\\d{3})", "### ### ###");
    }

    // The first regular expression checks for any phone number in the format of
    // +XX-XXXX-XXXX or (XXX) XXX-XXXX.
    // It allows for an optional +XX at the beginning, and spaces, dashes, and dots
    // between the parts of the phone number.
    // The second regular expression checks for any phone number in the format
    // +1-XXX-XXX-XXXX or (XXX) XXX-XXXX.
    // It allows for an optional +1 at the beginning, and spaces and dashes between
    // the parts of the phone number.
    // It also allows for an optional extension of up to four digits.
    private String cleanPhoneNumber(String content) {
        content = content.replaceAll("(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}",
                "# ### ### ###");
        content = content.replaceAll(
                "(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?",
                "# ### ### ###");
        return content;
    }

    // This regular expression checks for any valid email address in the format
    // username@domain.extension.
    // It allows for letters, numbers, underscores, hyphens and dots in the username
    // and domain, with a maximum of 5 characters for the extension.
    private String cleanEmailAddress(String content) {
        return content.replaceAll("([a-zA-Z0-9_\\-\\.]+)\\s*@([\\sa-zA-Z0-9_\\-\\.]+)[\\.\\,]([a-zA-Z]{1,5})",
                "####@####.####");
    }

    /*
     * Cleans the names of persons in the provided content by replacing them with '#' characters.
     * Uses StanfordCoreNLP for natural language processing and entity recognition.
     * Reverses the list of entity mentions to replace them from the end of the string,
     * preserving the indices of earlier mentions when replacing later ones.
     */
    public String cleanNames(String content) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument doc = new CoreDocument(content);

        pipeline.annotate(doc);

        List<CoreEntityMention> entityMentions = new ArrayList<>(doc.entityMentions());

        // Reverse the list to replace from the end of the string. This prevents messing up the start indices of
        // earlier entity mentions when replacing later ones.
        Collections.reverse(entityMentions);

        StringBuilder sb = new StringBuilder(content);

        for (CoreEntityMention em : entityMentions) {
            if (em.entityType().equals("PERSON")) {
                int start = em.charOffsets().first();
                int end = em.charOffsets().second();
                char[] replacement = new char[end - start];
                Arrays.fill(replacement, '#');
                sb.replace(start, end, new String(replacement));
            }
        }
        return sb.toString();
    }

}
