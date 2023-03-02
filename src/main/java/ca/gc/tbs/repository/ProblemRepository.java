package ca.gc.tbs.repository;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.data.mongodb.datatables.DataTablesRepository;
import org.springframework.data.mongodb.repository.Aggregation;
import ca.gc.tbs.domain.Problem;

public interface ProblemRepository extends DataTablesRepository<Problem, String> {
	List<Problem> findByAirTableSync(String syncd);

	List<Problem> findByProcessed(String processed);

	List<Problem> findByPersonalInfoProcessed(String processed);

	List<Problem> findByAutoTagProcessed(String processed);

	List<Problem> findByProcessedAndInstitution(String processed, String institution);

	@Aggregation(pipeline = { "{ '$group': { '_id' : '$url' } }" })
	DataTablesOutput<Problem> findDistinctUrls(@Valid DataTablesInput input);

}
