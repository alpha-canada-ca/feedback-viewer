package ca.gc.tbs.domain;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId("u1");
        user.setEmail("foo@bar.com");
        user.setPassword("pass");
        user.setInstitution("inst");
        user.setEnabled(true);
        user.setDateCreated("2024-01-01");

        Role role = new Role();
        role.setId("r1");
        role.setRole("ADMIN");
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        assertEquals("u1", user.getId());
        assertEquals("foo@bar.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("inst", user.getInstitution());
        assertTrue(user.isEnabled());
        assertEquals("2024-01-01", user.getDateCreated());
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void testSetEnabledFalse() {
        User user = new User();
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void testSetRolesNull() {
        User user = new User();
        user.setRoles(null);
        assertNull(user.getRoles());
    }

}