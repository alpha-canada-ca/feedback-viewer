package ca.gc.tbs.repository;

import ca.gc.tbs.domain.TopTaskSurvey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomTopTaskRepositoryImplTest {

    private MongoTemplate mongoTemplate;
    private CustomTopTaskRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        repository = new CustomTopTaskRepositoryImpl(mongoTemplate);
    }

    @Test
    void findDistinctTaskCountsWithFilters_returnsResults() {
        Criteria criteria = new Criteria();

        Map<String, Object> sampleMap = new HashMap<>();
        sampleMap.put("task", "Sample Task");

        List<Map> expectedList = Collections.singletonList(sampleMap);

        @SuppressWarnings("unchecked")
        AggregationResults<Map> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(expectedList);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq(TopTaskSurvey.class), eq(Map.class)))
                .thenReturn(aggResults);

        List<Map> result = repository.findDistinctTaskCountsWithFilters(criteria);

        assertEquals(expectedList, result);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq(TopTaskSurvey.class), eq(Map.class));
    }
}

