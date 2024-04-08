package ca.gc.tbs.repository;

import org.springframework.data.mongodb.core.query.Criteria;
import java.util.List;
import java.util.Map;

public interface CustomTopTaskRepository {
    List<Map> findDistinctTaskCountsWithFilters(Criteria criteria);
}
