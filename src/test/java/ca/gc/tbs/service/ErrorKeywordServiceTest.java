package ca.gc.tbs.service;

import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ErrorKeywordServiceTest {

    ErrorKeywordService errorKeywordService;

    @BeforeEach
    void setup() {
        errorKeywordService = new ErrorKeywordService();
        errorKeywordService.init();
    }

    @Test
    void testContainsErrorKeywords_bilingual() {
        assertFalse(errorKeywordService.containsErrorKeywords("unmatchableword", "en"));
    }

    @Test
    void testContainsErrorKeywords_english() {
        assertFalse(errorKeywordService.containsErrorKeywords("unmatchableword", "en"));
    }

    @Test
    void testContainsErrorKeywords_french() {
        assertFalse(errorKeywordService.containsErrorKeywords("unmatchableword", "fr"));
    }

    @Test
    void testContainsErrorKeywords_empty() {
        assertFalse(errorKeywordService.containsErrorKeywords("", "en"));
    }

}