package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

  Role findByRole(String role);
}
