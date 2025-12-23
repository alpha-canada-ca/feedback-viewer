package ca.gc.tbs.repository;

import ca.gc.tbs.domain.OriginalProblem;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OriginalProblemRepository extends DataTablesRepository<OriginalProblem, String> {}
