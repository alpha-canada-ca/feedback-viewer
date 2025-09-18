package ca.gc.tbs.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryCustomImplTest {

    private MongoTemplate mongoTemplate;
    private UserRepositoryCustomImpl repository;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        repository = new UserRepositoryCustomImpl();

        repository.mongoTemplate = mongoTemplate;
    }

    @Test
    void findAllInstitutions_returnsList() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class, RETURNS_DEEP_STUBS);
        UserRepositoryCustomImpl repository = new UserRepositoryCustomImpl();
        repository.mongoTemplate = mongoTemplate;

        List<String> expected = Arrays.asList("Inst1", "Inst2");

        when(
                mongoTemplate
                        .query(any(Class.class))
                        .distinct(eq("institution"))
                        .as(eq(String.class))
                        .all()
        ).thenReturn(expected);


        List<String> result = repository.findAllInstitutions();

        assertEquals(expected, result);
    }
}
