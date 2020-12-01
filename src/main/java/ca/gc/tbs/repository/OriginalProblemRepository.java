package ca.gc.tbs.repository;

import org.springframework.data.mongodb.datatables.DataTablesRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

import ca.gc.tbs.domain.OriginalProblem;


public interface OriginalProblemRepository extends DataTablesRepository<OriginalProblem, String> {
	
}
