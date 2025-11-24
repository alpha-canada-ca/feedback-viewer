package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

  User findByEmail(String email);

  List<User> findByRolesContaining(Role role);
}
