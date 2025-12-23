package ca.gc.tbs.repository;

import ca.gc.tbs.domain.TopTaskSurvey;
import java.util.List;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopTaskRepository
    extends DataTablesRepository<TopTaskSurvey, String>, CustomTopTaskRepository {

  List<TopTaskSurvey> findByTopTaskAirTableSync(String syncd);

  List<TopTaskSurvey> findByProcessed(String processed);

  List<TopTaskSurvey> findByPersonalInfoProcessed(String processed);

  List<TopTaskSurvey> findByAutoTagProcessed(String processed);

  @Query("SELECT DISTINCT t.task FROM TopTaskSurvey t WHERE t.processed = 'true' AND LOWER(t.task) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY t.task")
  List<String> findTaskTitlesBySearch(@Param("search") String search);

  @Query("SELECT DISTINCT t.task FROM TopTaskSurvey t WHERE t.processed = 'true'")
  List<String> findDistinctTaskNames();
}
