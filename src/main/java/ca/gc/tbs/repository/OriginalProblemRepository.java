package ca.gc.tbs.repository;

import ca.gc.tbs.domain.OriginalProblem;
import org.springframework.data.mongodb.datatables.DataTablesRepository;

public interface OriginalProblemRepository extends DataTablesRepository<OriginalProblem, String> {}
