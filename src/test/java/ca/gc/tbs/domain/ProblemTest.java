package ca.gc.tbs.domain;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class ProblemTest {
    @Test
    void testAllArgsConstructorAndGetters() {
        Problem problem = new Problem(
                "pid", "http://example.com", 3, "Mobile", "Firefox",
                "2023-01-01", "2023-01-01T12:00:00", "details", "fr", "title",
                "institution", "theme", "section", "en", "contact"
        );
        assertEquals("pid", problem.getId());
        assertEquals("http://example.com", problem.getUrl());
        assertEquals(3, problem.getUrlEntries());
        assertEquals("Mobile", problem.getDeviceType());
        assertEquals("Firefox", problem.getBrowser());
        assertEquals("details", problem.getProblemDetails());
        assertEquals("2023-01-01", problem.getProblemDate());
        assertEquals("2023-01-01T12:00:00", problem.getTimeStamp());
        assertEquals("fr", problem.getLanguage());
        assertEquals("title", problem.getTitle());
        assertEquals("institution", problem.getInstitution());
        assertEquals("theme", problem.getTheme());
        assertEquals("section", problem.getSection());
        assertEquals("en", problem.getOppositeLang());
        assertEquals("contact", problem.getContact());
        assertNotNull(problem.getTags());
        assertTrue(problem.getTags().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        Problem problem = new Problem();
        problem.setId("id");
        problem.setUrl("url");
        problem.setUrlEntries(5);
        problem.setDeviceType("PC");
        problem.setBrowser("Chrome");
        problem.setProblemDetails("details");
        problem.setProblemDate("2024-01-01");
        problem.setTimeStamp("12:00:00");
        problem.setLanguage("en");
        problem.setTitle("title");
        problem.setInstitution("inst");
        problem.setTheme("theme");
        problem.setSection("sec");
        problem.setOppositeLang("fr");
        problem.setContact("contact");
        problem.setTags(Arrays.asList("tag1", "tag2"));
        problem.setProcessed("Y");
        problem.setAirTableSync("Y");
        problem.setDataOrigin("origin");
        problem.setPersonalInfoProcessed("Y");
        problem.setAutoTagProcessed("Y");
        problem.setProcessedDate("2024-02-02");

        assertEquals("id", problem.getId());
        assertEquals("url", problem.getUrl());
        assertEquals(5, problem.getUrlEntries());
        assertEquals("PC", problem.getDeviceType());
        assertEquals("Chrome", problem.getBrowser());
        assertEquals("details", problem.getProblemDetails());
        assertEquals("2024-01-01", problem.getProblemDate());
        assertEquals("12:00:00", problem.getTimeStamp());
        assertEquals("en", problem.getLanguage());
        assertEquals("title", problem.getTitle());
        assertEquals("inst", problem.getInstitution());
        assertEquals("theme", problem.getTheme());
        assertEquals("sec", problem.getSection());
        assertEquals("fr", problem.getOppositeLang());
        assertEquals("contact", problem.getContact());
        assertEquals(Arrays.asList("tag1", "tag2"), problem.getTags());
        assertEquals("Y", problem.getProcessed());
        assertEquals("Y", problem.getAirTableSync());
        assertEquals("origin", problem.getDataOrigin());
        assertEquals("Y", problem.getPersonalInfoProcessed());
        assertEquals("Y", problem.getAutoTagProcessed());
        assertEquals("2024-02-02", problem.getProcessedDate());
    }

    @Test
    void testCopyConstructorDeepCopyTags() {
        Problem p1 = new Problem();
        p1.setId("id");
        p1.setTags(Arrays.asList("a", "b"));
        Problem p2 = new Problem(p1);
        assertEquals("id", p2.getId());
        assertEquals(Arrays.asList("a", "b"), p2.getTags());
        // Ensure tags is a deep copy
        p2.getTags().add("c");
        assertEquals(2, p1.getTags().size());
        assertEquals(3, p2.getTags().size());
    }

    @Test
    void testSetTagsNull() {
        Problem problem = new Problem();
        problem.setTags(null);
        assertNull(problem.getTags());
    }
}
