package ca.gc.tbs.service;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.domain.User;
import ca.gc.tbs.repository.RoleRepository;
import ca.gc.tbs.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUserByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        assertEquals(user, userService.findUserByEmail("test@example.com"));
    }

    @Test
    void testSaveUser_firstUserIsAdmin() {
        User user = new User();
        when(userRepository.count()).thenReturn(0L);
        Role adminRole = new Role();
        adminRole.setRole(UserService.ADMIN_ROLE);
        when(roleRepository.findByRole(UserService.ADMIN_ROLE)).thenReturn(adminRole);
        when(bCryptPasswordEncoder.encode(any())).thenReturn("hashed");
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            assertTrue(u.isEnabled());
            assertEquals(adminRole, u.getRoles().iterator().next());
            return null;
        }).when(userRepository).save(any(User.class));
        user.setPassword("pw");
        userService.saveUser(user);
    }

    @Test
    void testIsAdmin() {
        Role adminRole = new Role();
        adminRole.setRole(UserService.ADMIN_ROLE);
        User user = new User();
        user.setRoles(Collections.singleton(adminRole));
        assertTrue(userService.isAdmin(user));
    }

    @Test
    void testFindUserById_userPresent() {
        User user = new User();
        user.setId("id123");
        when(userRepository.findById("id123")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findUserById("id123"));
    }
    @Test
    void testFindUserById_userAbsent_throwsException() {
        when(userRepository.findById("nope")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.findUserById("nope"));
    }

    @Test
    void testFindUserByRole_roleMissing() {
        when(roleRepository.findByRole("MISSING_ROLE")).thenReturn(null);
        when(userRepository.findByRolesContaining(null)).thenReturn(Collections.emptyList());
        List<User> users = userService.findUserByRole("MISSING_ROLE");
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testDeleteUserById() {
        doNothing().when(userRepository).deleteById("id123");
        assertDoesNotThrow(() -> userService.deleteUserById("id123"));
        verify(userRepository, times(1)).deleteById("id123");
    }

    @Test
    void testFindAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(new User(), new User()));
        assertEquals(2, userService.findAllUsers().size());
    }

    @Test
    void testFindInstitutions() {
        List<String> institutions = Arrays.asList("A", "B");
        when(userRepository.findAllInstitutions()).thenReturn(institutions);
        assertEquals(institutions, userService.findInstitutions());
    }

    @Test
    void testLoadUserByUsername_userNotFound() {
        when(userRepository.findByEmail("nobody@nowhere.com")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("nobody@nowhere.com"));
    }

    @Test
    void testFindUserById_optionalEmpty_throws() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.findUserById("missing"));
    }

}