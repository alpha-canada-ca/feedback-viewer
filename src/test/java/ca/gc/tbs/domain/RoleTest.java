package ca.gc.tbs.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {
    @Test
    void testGettersAndSetters() {
        Role role = new Role();
        role.setId("r2");
        role.setRole("USER");
        assertEquals("r2", role.getId());
        assertEquals("USER", role.getRole());
    }
}
