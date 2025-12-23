package ca.gc.tbs.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.Predicate;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import ca.gc.tbs.domain.Problem;

@Repository
public class CustomProblemRepositoryImpl implements CustomProblemRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  @Lazy
  private ProblemRepository problemRepository;

  @Override
  public DataTablesOutput<Problem> findAllWithErrorKeywords(
      @Valid DataTablesInput input, Set<String> keywords, Specification<Problem> baseSpec) {
    
    if (!keywords.isEmpty()) {
      // Create a specification that combines base spec with keyword search
      Specification<Problem> keywordSpec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();
        for (String keyword : keywords) {
          predicates.add(cb.like(cb.lower(root.get("problemDetails")), 
              "%" + keyword.toLowerCase() + "%"));
        }
        return cb.or(predicates.toArray(new Predicate[0]));
      };
      
      Specification<Problem> combinedSpec = baseSpec != null 
          ? baseSpec.and(keywordSpec) 
          : keywordSpec;
      
      return problemRepository.findAll(input, combinedSpec);
    }
    
    return problemRepository.findAll(input, baseSpec);
  }

  @Override
  public List<Map<String, Object>> findDistinctUrlsWithDetails() {
    String jpql = "SELECT p.url as url, SUBSTRING(p.problemDate, 1, 10) as day, " +
        "COUNT(p) as count, p.institution as institution, p.title as title, " +
        "p.problemDate as problemDate, p.language as language, " +
        "p.section as section, p.theme as theme " +
        "FROM Problem p WHERE p.processed = 'true' " +
        "GROUP BY p.url, SUBSTRING(p.problemDate, 1, 10), p.institution, p.title, " +
        "p.problemDate, p.language, p.section, p.theme";
    
    List<Tuple> results = entityManager.createQuery(jpql, Tuple.class).getResultList();
    List<Map<String, Object>> mappedResults = new ArrayList<>();
    
    for (Tuple tuple : results) {
      Map<String, Object> row = new HashMap<>();
      row.put("url", tuple.get("url"));
      row.put("day", tuple.get("day"));
      row.put("count", tuple.get("count"));
      row.put("institution", tuple.get("institution"));
      row.put("title", tuple.get("title"));
      row.put("problemDate", tuple.get("problemDate"));
      row.put("language", tuple.get("language"));
      row.put("section", tuple.get("section"));
      row.put("theme", tuple.get("theme"));
      mappedResults.add(row);
    }
    
    return mappedResults;
  }

  @Override
  public Map<String, Object> findEarliestAndLatestProblemDate() {
    String jpql = "SELECT MIN(p.problemDate) as earliestDate, MAX(p.problemDate) as latestDate " +
        "FROM Problem p WHERE p.processed = 'true'";
    
    Tuple result = entityManager.createQuery(jpql, Tuple.class).getSingleResult();
    
    Map<String, Object> dateRange = new HashMap<>();
    dateRange.put("earliestDate", result.get("earliestDate"));
    dateRange.put("latestDate", result.get("latestDate"));
    
    return dateRange;
  }
}
