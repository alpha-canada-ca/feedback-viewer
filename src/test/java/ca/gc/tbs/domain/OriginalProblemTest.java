package ca.gc.tbs.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OriginalProblemTest {
    @Test
    void testOriginalProblemIsProblem() {
        OriginalProblem op = new OriginalProblem();
        op.setId("oid");
        op.setUrl("url");
        assertEquals("oid", op.getId());
        assertEquals("url", op.getUrl());
    }

    @Test
    void testOriginalProblemNullUrl() {
        OriginalProblem op = new OriginalProblem();
        op.setUrl(null);
        assertNull(op.getUrl());
    }
}
