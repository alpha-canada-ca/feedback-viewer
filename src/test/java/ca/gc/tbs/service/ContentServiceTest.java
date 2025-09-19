package ca.gc.tbs.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ContentServiceTest {

    ContentService contentService;

    @BeforeEach
    void setup() {
        BadWords.loadConfigs();
        contentService = new ContentService();
    }

    @Test
    void testCleanContent_empty() {
        assertEquals("", contentService.cleanContent(""));
    }

    @Test
    void testCleanPostalCode() {
        String input = "My code is K1A 0B1";
        String result = contentService.cleanContent(input);
        assertTrue(result.contains("### ###"));
    }

    @Test
    void testCleanPhoneNumber() {
        String input = "My number is 613-555-1234";
        String result = contentService.cleanContent(input);
        assertTrue(result.contains("# ### ### ###"));
    }

    @Test
    void testCleanEmailAddress() {
        String input = "Contact: test@example.com";
        String result = contentService.cleanContent(input);
        assertTrue(result.contains("####@####.####"));
    }

    @Test
    void testCleanPassportNumber() {
        String input = "My passport number is AB123456";
        String result = contentService.cleanContent(input);
        assertTrue(result.contains("## ######"));
    }

    @Test
    void testCleanSIN() {
        String input = "My SIN is 123 456 789";
        String result = contentService.cleanContent(input);
        assertTrue(result.contains("### ### ###"));
    }

    // For cleanNames, you could mock CoreNLP - or just check that, for a basic string, it doesn't throw
    @Test
    void testCleanNames_noError() {
        String input = "John Doe went to Ottawa.";
        String result = contentService.cleanNames(input);
        assertNotNull(result);
    }
}