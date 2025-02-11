
package ca.gc.tbs.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ErrorKeywordService {
    private Set<String> englishKeywords = new HashSet<>();
    private Set<String> frenchKeywords = new HashSet<>();
    private Set<String> bilingualKeywords = new HashSet<>();

    public Set<String> getEnglishKeywords() {
        return englishKeywords;
    }

    public Set<String> getFrenchKeywords() {
        return frenchKeywords;
    }

    public Set<String> getBilingualKeywords() {
        return bilingualKeywords;
    }

    @PostConstruct
    public void init() {
        try {
            LOG.info("Starting to load error keywords...");

            englishKeywords = loadKeywords("static/error_keywords/errors_en.txt");
            LOG.info("Loaded English keywords. Size: {}, Sample: {}",
                    englishKeywords.size(),
                    new ArrayList<>(englishKeywords).subList(0, Math.min(5, englishKeywords.size())));

            frenchKeywords = loadKeywords("static/error_keywords/errors_fr.txt");
            LOG.info("Loaded French keywords. Size: {}, Sample: {}",
                    frenchKeywords.size(),
                    new ArrayList<>(frenchKeywords).subList(0, Math.min(5, frenchKeywords.size())));

            bilingualKeywords = loadKeywords("static/error_keywords/errors_bilingual.txt");
            LOG.info("Loaded Bilingual keywords. Size: {}, Sample: {}",
                    bilingualKeywords.size(),
                    new ArrayList<>(bilingualKeywords).subList(0, Math.min(5, bilingualKeywords.size())));

            LOG.info("Successfully loaded all keyword sets. Total keywords - English: {}, French: {}, Bilingual: {}",
                    englishKeywords.size(), frenchKeywords.size(), bilingualKeywords.size());
        } catch (Exception e) {
            LOG.error("Failed to initialize error keywords", e);
            throw e;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ErrorKeywordService.class);

    private Set<String> loadKeywords(String path) {
        Set<String> keywords = new HashSet<>();
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        keywords.add(line.trim().toLowerCase());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load error keywords from " + path, e);
        }
        return keywords;
    }

    public boolean containsErrorKeywords(String text, String language) {
        if (text == null || text.isEmpty()) {
            LOG.debug("Text is null or empty");
            return false;
        }

        text = text.toLowerCase();
        LOG.debug("Checking text for error keywords: {}", text);
        LOG.debug("Language: {}", language);

        // Check bilingual keywords first
        LOG.debug("Checking {} bilingual keywords", bilingualKeywords.size());
        for (String keyword : bilingualKeywords) {
            if (containsWord(text, keyword)) {
                LOG.debug("Found bilingual keyword match: {}", keyword);
                return true;
            }
        }

        // Check language-specific keywords
        Set<String> languageKeywords = "fr".equalsIgnoreCase(language) ? frenchKeywords : englishKeywords;
        LOG.debug("Checking {} {} keywords", languageKeywords.size(), language);

        for (String keyword : languageKeywords) {
            if (containsWord(text, keyword)) {
                LOG.debug("Found {} keyword match: {}", language, keyword);
                return true;
            }
        }

        LOG.debug("No error keywords found in text");
        return false;
    }

    private boolean containsWord(String text, String keyword) {
        // Use a more flexible pattern that doesn't require strict word boundaries
        // This allows matching phrases within larger text
        String pattern = "(?i)" + Pattern.quote(keyword);
        return Pattern.compile(pattern).matcher(text).find();
    }
}