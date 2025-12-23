package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

  Role findByRole(String role);
}
