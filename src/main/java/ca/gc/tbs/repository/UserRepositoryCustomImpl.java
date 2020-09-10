package ca.gc.tbs.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import ca.gc.tbs.domain.Problem;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	@Autowired
	MongoTemplate mongoTemplate;

	public UserRepositoryCustomImpl() {

	}

	public List<String> findAllInstitutions() {
		List<String> instList = mongoTemplate.query(Problem.class).distinct("institution").as(String.class).all();
		return instList;
	}

}
