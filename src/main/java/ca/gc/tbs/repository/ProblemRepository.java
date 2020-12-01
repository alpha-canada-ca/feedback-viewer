package ca.gc.tbs.repository;

import java.util.List;

import org.springframework.data.mongodb.datatables.DataTablesRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import ca.gc.tbs.domain.Problem;


public interface ProblemRepository extends DataTablesRepository<Problem, String> {
	List<Problem> findByAirTableSync(String syncd);
	List<Problem> findByProcessed(String processed);
	List<Problem> findByPersonalInfoProcessed(String processed);
	List<Problem> findByAutoTagProcessed(String processed);
	List<Problem> findByProcessedAndInstitution(String processed, String institution);
}
