package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>, UserRepositoryCustom {

  User findByEmail(String email);

  @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
  List<User> findByRolesContaining(Role role);
}
