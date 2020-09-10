package ca.gc.tbs.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ca.gc.tbs.domain.Role;


public interface RoleRepository extends MongoRepository<Role, String> {
    
    Role findByRole(String role);
}
