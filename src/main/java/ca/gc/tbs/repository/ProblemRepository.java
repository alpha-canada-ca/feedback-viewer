package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Problem;
import java.util.List;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends DataTablesRepository<Problem, String>, CustomProblemRepository {

  List<Problem> findByAirTableSync(String syncd);

  List<Problem> findByProcessed(String processed);

  List<Problem> findByPersonalInfoProcessed(String processed);

  List<Problem> findByAutoTagProcessed(String processed);

  @Query("SELECT p FROM Problem p WHERE p.processed = 'true'")
  List<Problem> findAllProcessedProblems();

  List<Problem> findByProcessedAndInstitution(String processed, String institution);

  @Query("SELECT DISTINCT p.title FROM Problem p WHERE p.processed = 'true' ORDER BY p.title")
  List<String> findDistinctPageNames();

  @Query("SELECT DISTINCT p.title FROM Problem p WHERE p.processed = 'true' AND LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY p.title")
  List<String> findPageTitlesBySearch(@Param("search") String search);

  @Query("SELECT DISTINCT p.url FROM Problem p WHERE p.processed = 'true'")
  List<String> findDistinctProcessedUrls();
}
