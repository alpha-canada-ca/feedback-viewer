package ca.gc.tbs.repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  public UserRepositoryCustomImpl() {}

  @Override
  public List<String> findAllInstitutions() {
    return entityManager
        .createQuery("SELECT DISTINCT p.institution FROM Problem p WHERE p.institution IS NOT NULL ORDER BY p.institution", String.class)
        .getResultList();
  }
}
