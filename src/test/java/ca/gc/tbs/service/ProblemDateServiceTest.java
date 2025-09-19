package ca.gc.tbs.service;

import org.junit.jupiter.api.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProblemDateServiceTest {

    ProblemDateService problemDateService;

    @BeforeEach
    void setup() {
        problemDateService = new ProblemDateService();
    }

    @Test
    void testGetProblemDates() {
        Map<String, String> dates = problemDateService.getProblemDates();
        assertTrue(dates.containsKey("earliestDate"));
        assertTrue(dates.containsKey("latestDate"));
    }

    @Test
    void testGetProblemDates_extremePast() {
        ProblemDateService service = new ProblemDateService();
        Map<String, String> result = service.getProblemDates();
        assertNotNull(result.get("earliestDate"));
        assertNotNull(result.get("latestDate"));
    }
}
