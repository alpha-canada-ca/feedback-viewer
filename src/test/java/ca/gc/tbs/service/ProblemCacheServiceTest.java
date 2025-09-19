package ca.gc.tbs.service;

import ca.gc.tbs.repository.ProblemRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProblemCacheServiceTest {

    @Mock
    ProblemRepository problemRepository;

    @InjectMocks
    ProblemCacheService problemCacheService;


    private void setPrivateProblemRepository(ProblemCacheService service, ProblemRepository repo) throws Exception {
        Field field = ProblemCacheService.class.getDeclaredField("problemRepository");
        field.setAccessible(true);
        field.set(service, repo);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProcessedProblems() {
        when(problemRepository.findAllProcessedProblems()).thenReturn(Collections.emptyList());
        assertNotNull(problemCacheService.getProcessedProblems());
    }

    @Test
    void testGetDistinctProcessedUrlsForCache() {
        when(problemRepository.findDistinctProcessedUrls()).thenReturn(Collections.singletonList("url"));
        assertEquals(1, problemCacheService.getDistinctProcessedUrlsForCache().size());
    }

    @Test
    void testGetProcessedProblems_repositoryThrows() throws Exception {
        ProblemRepository repo = mock(ProblemRepository.class);
        when(repo.findAllProcessedProblems()).thenThrow(new RuntimeException("DB error"));
        ProblemCacheService service = new ProblemCacheService();
        setPrivateProblemRepository(service, repo);
        assertThrows(RuntimeException.class, service::getProcessedProblems);
    }

    @Test
    void testGetDistinctProcessedUrlsForCache_emptyList() throws Exception {
        ProblemRepository repo = mock(ProblemRepository.class);
        when(repo.findDistinctProcessedUrls()).thenReturn(Collections.emptyList());
        ProblemCacheService service = new ProblemCacheService();
        setPrivateProblemRepository(service, repo);
        assertTrue(service.getDistinctProcessedUrlsForCache().isEmpty());
    }
}