package ca.gc.tbs.repository;

import ca.gc.tbs.domain.TopTaskSurvey; // Import your domain class
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

public class CustomTopTaskRepositoryImpl implements CustomTopTaskRepository {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public CustomTopTaskRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<Map> findDistinctTaskCountsWithFilters(Criteria criteria) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(criteria), // Apply the filter criteria
            Aggregation.project("task"), // Include only the 'task' field in the projection
            Aggregation.group(
                "task") // Group by the 'task' field, effectively getting distinct tasks
            );

    // Assuming 'TopTaskSurvey' is the domain class representing your collection
    AggregationResults<Map> results =
        mongoTemplate.aggregate(aggregation, TopTaskSurvey.class, Map.class);
    return results.getMappedResults();
  }
}
