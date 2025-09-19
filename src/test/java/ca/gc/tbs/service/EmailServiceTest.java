package ca.gc.tbs.service;

import ca.gc.tbs.domain.User;
import org.junit.jupiter.api.*;
import org.mockito.*;
import uk.gov.service.notify.NotificationClient;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @InjectMocks
    EmailService emailService = new EmailService();

    @Mock
    UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        emailService.setAccountEnabledKey("test-account-key");
        emailService.setUserActivationRequestKey("test-user-key");
    }

    @Test
    void testGetUserActivationRequestKey() {
        assertEquals("test-user-key", emailService.getUserActivationRequestKey());
    }

    @Test
    void testSendUserActivationRequestEmail() {
        User admin = new User();
        admin.setEmail("admin@example.com");
        when(userService.findUserByRole(UserService.ADMIN_ROLE)).thenReturn(Collections.singletonList(admin));
        EmailService spyService = spy(emailService);
        doReturn(mock(NotificationClient.class)).when(spyService).getNotificationClient();

        spyService.sendUserActivationRequestEmail("user@example.com");

        verify(userService, times(1)).findUserByRole(UserService.ADMIN_ROLE);
    }

}