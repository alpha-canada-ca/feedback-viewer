package ca.gc.tbs;

import ca.gc.tbs.domain.Role;
import ca.gc.tbs.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PageSuccessApplicationTest {

    @Test
    void init_createsMissingRoles() throws Exception {
        RoleRepository repository = mock(RoleRepository.class);

        when(repository.findByRole("ADMIN")).thenReturn(null);
        when(repository.findByRole("USER")).thenReturn(null);
        when(repository.findByRole("API")).thenReturn(null);

        PageSuccessApplication app = new PageSuccessApplication();

        app.init(repository).run();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(repository, times(3)).save(captor.capture());

        assertEquals("ADMIN", captor.getAllValues().get(0).getRole());
        assertEquals("USER", captor.getAllValues().get(1).getRole());
        assertEquals("API", captor.getAllValues().get(2).getRole());
    }

    @Test
    void init_doesNotCreateExistingRoles() throws Exception {
        RoleRepository repository = mock(RoleRepository.class);

        when(repository.findByRole("ADMIN")).thenReturn(new Role());
        when(repository.findByRole("USER")).thenReturn(new Role());
        when(repository.findByRole("API")).thenReturn(new Role());

        PageSuccessApplication app = new PageSuccessApplication();

        app.init(repository).run();

        verify(repository, never()).save(any());
    }
}

