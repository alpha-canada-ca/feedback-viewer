package ca.gc.tbs.repository;

import java.util.List;
import org.springframework.data.mongodb.datatables.DataTablesRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
import ca.gc.tbs.domain.TopTaskSurvey;

public interface TopTaskRepository extends DataTablesRepository<TopTaskSurvey, String> {
	List<TopTaskSurvey> findByTopTaskAirTableSync(String syncd);

	List<TopTaskSurvey> findByProcessed(String processed);

	List<TopTaskSurvey> findByPersonalInfoProcessed(String processed);

	List<TopTaskSurvey> findByAutoTagProcessed(String processed);
	// List<TopTaskSurvey> findByProcessedAndInstitution(String processed);
}