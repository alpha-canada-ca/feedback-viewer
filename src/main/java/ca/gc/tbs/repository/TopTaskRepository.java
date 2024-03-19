package ca.gc.tbs.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.datatables.DataTablesRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
import ca.gc.tbs.domain.TopTaskSurvey;
import org.springframework.data.mongodb.repository.Aggregation;

public interface TopTaskRepository extends DataTablesRepository<TopTaskSurvey, String> {
	List<TopTaskSurvey> findByTopTaskAirTableSync(String syncd);

	List<TopTaskSurvey> findByProcessed(String processed);

	List<TopTaskSurvey> findByPersonalInfoProcessed(String processed);

	List<TopTaskSurvey> findByAutoTagProcessed(String processed);


	@Aggregation(pipeline = {
			"{ '$match': { 'processed': 'true', 'task': { '$regex': ?0, '$options': 'i' } } }",
			"{ '$group': { '_id': '$task' } }",
			"{ '$sort': { '_id': 1 } }"
	})
	List<String> findTaskTitlesBySearch(String search);

	@Aggregation(pipeline = {
			"{ '$match': { 'processed': 'true' } }", // Optional, adjust based on your requirements
			"{ '$group': { '_id': 'task' } }",
			"{ '$sort': { '_id': 1 } }" // Optional, sorts the page names alphabetically
	})
	List<String> findDistinctTaskNames();

	@Aggregation(pipeline = {
			// Match stage to filter documents (optional, adjust based on your requirements)
			"{ '$match': { 'processed': 'true' } }",
			"{ '$group': { '_id': '$taskName', 'dept': { '$first': '$dept' } } }",
			"{ '$project': { 'taskName': '$_id', 'dept': 1, '_id': 0 } }",
			"{ '$sort': { 'taskName': 1 } }"
	})
	AggregationResults<Map> findDistinctTaskNamesAndDepts();


}