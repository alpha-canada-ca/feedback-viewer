package ca.gc.tbs.repository;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import ca.gc.tbs.domain.TopTaskSurvey;

public interface CustomTopTaskRepository {
  List<Map<String, Object>> findDistinctTaskCountsWithFilters(Specification<TopTaskSurvey> spec);
}
