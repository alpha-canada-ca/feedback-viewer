package ca.gc.tbs.repository;

import ca.gc.tbs.domain.BadWordEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadWordEntryRepository extends MongoRepository<BadWordEntry, String> {

    List<BadWordEntry> findByTypeAndActive(String type, boolean active);

    long countByType(String type);
}
