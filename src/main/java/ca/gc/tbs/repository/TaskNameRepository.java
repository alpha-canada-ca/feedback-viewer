package ca.gc.tbs.repository;

import ca.gc.tbs.domain.TaskName;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TaskNameRepository extends MongoRepository<TaskName, String> {
    Optional<TaskName> findByName(String name);
}
