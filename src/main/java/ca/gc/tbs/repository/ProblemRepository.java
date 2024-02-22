package ca.gc.tbs.repository;

import ca.gc.tbs.domain.Problem;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.datatables.DataTablesInput;
import org.springframework.data.mongodb.datatables.DataTablesOutput;
import org.springframework.data.mongodb.datatables.DataTablesRepository;
import org.springframework.data.mongodb.repository.Aggregation;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface ProblemRepository extends DataTablesRepository<Problem, String> {
    List<Problem> findByAirTableSync(String syncd);

    List<Problem> findByProcessed(String processed);

    List<Problem> findByPersonalInfoProcessed(String processed);

    List<Problem> findByAutoTagProcessed(String processed);


    List<Problem> findByProcessedAndInstitution(String processed, String institution);

    @Aggregation(pipeline = {"{ '$group': { '_id' : '$url' } }"})
    DataTablesOutput<Problem> findDistinctUrls(@Valid DataTablesInput input);

    // New method to find the earliest and latest problemDate

    @Aggregation(pipeline = {
            "{ '$group': { '_id': null, 'earliestDate': { '$min': '$problemDate' }, 'latestDate': { '$max': '$problemDate' } } }",
            "{ '$project': { '_id': 0, 'earliestDate': 1, 'latestDate': 1 } }"
    })
    AggregationResults<Map> findEarliestAndLatestProblemDate();

    @Aggregation(pipeline = {
            "{ '$match': { 'processed': 'true' } }", // Optional, adjust based on your requirements
            "{ '$group': { '_id': '$title' } }",
            "{ '$sort': { '_id': 1 } }" // Optional, sorts the page names alphabetically
    })
    List<String> findDistinctPageNames();

    @Aggregation(pipeline = {
            "{ '$match': { 'processed': 'true', 'title': { '$regex': ?0, '$options': 'i' } } }",
            "{ '$group': { '_id': '$title' } }",
            "{ '$sort': { '_id': 1 } }"
    })
    List<String> findPageTitlesBySearch(String search);
}
