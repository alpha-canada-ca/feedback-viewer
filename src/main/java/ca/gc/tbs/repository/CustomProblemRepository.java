package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Problem;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;

public interface CustomProblemRepository {
  
  DataTablesOutput<Problem> findAllWithErrorKeywords(
      @Valid DataTablesInput input, Set<String> keywords, Specification<Problem> baseSpec);
  
  List<Map<String, Object>> findDistinctUrlsWithDetails();
  
  Map<String, Object> findEarliestAndLatestProblemDate();
}
