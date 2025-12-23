package ca.gc.tbs.repository;

import ca.gc.tbs.domain.TopTaskSurvey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class CustomTopTaskRepositoryImpl implements CustomTopTaskRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<Map<String, Object>> findDistinctTaskCountsWithFilters(Specification<TopTaskSurvey> spec) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Tuple> query = cb.createTupleQuery();
    Root<TopTaskSurvey> root = query.from(TopTaskSurvey.class);

    // Apply the specification (filter criteria)
    if (spec != null) {
      Predicate predicate = spec.toPredicate(root, query, cb);
      if (predicate != null) {
        query.where(predicate);
      }
    }

    // Group by task and select task as _id (to match MongoDB result format)
    query.multiselect(root.get("task").alias("_id"));
    query.groupBy(root.get("task"));

    List<Tuple> results = entityManager.createQuery(query).getResultList();
    List<Map<String, Object>> mappedResults = new ArrayList<>();

    for (Tuple tuple : results) {
      Map<String, Object> row = new HashMap<>();
      row.put("_id", tuple.get("_id"));
      mappedResults.add(row);
    }

    return mappedResults;
  }
}
