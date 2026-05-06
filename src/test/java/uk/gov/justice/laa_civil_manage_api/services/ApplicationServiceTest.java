package uk.gov.justice.laa_civil_manage_api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa_civil_manage_api.clients.DataAccessApiClient;
import uk.gov.justice.laa_civil_manage_api.models.Application;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private DataAccessApiClient dataAccessApiClient;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void shouldReturnAllApplications() {
        Application app1 = Application.builder()
                .applicationId("1")
                .clientFirstName("Janet")
                .status("PENDING")
                .build();

        Application app2 = Application.builder()
                .applicationId("2")
                .clientFirstName("Bob")
                .status("PENDING")
                .build();

        List<Application> expectedApplications = List.of(app1, app2);

        when(dataAccessApiClient.getApplications()).thenReturn(expectedApplications);

        List<Application> actualApplications = applicationService.getApplications();

        assertEquals(2, actualApplications.size());
        assertEquals(expectedApplications, actualApplications);
        verify(dataAccessApiClient, times(1)).getApplications();
    }

    @Test
    void shouldReturnApplicationWhenIdExists() {
        Application app1 = Application.builder()
                .applicationId("1")
                .clientFirstName("Janet")
                .status("PENDING")
                .build();

        Application app2 = Application.builder()
                .applicationId("2")
                .clientFirstName("Bob")
                .status("PENDING")
                .build();

        when(dataAccessApiClient.getApplications()).thenReturn(List.of(app1, app2));

        Application result = applicationService.getApplicationById("2");

        assertNotNull(result);
        assertEquals("2", result.applicationId());
        assertEquals("Bob", result.clientFirstName());
    }

    @Test
    void shouldThrowExceptionWhenIdDoesNotExist() {
        Application app1 = Application.builder()
                .applicationId("1")
                .clientFirstName("Janet")
                .status("PENDING")
                .build();

        Application app2 = Application.builder()
                .applicationId("2")
                .clientFirstName("Bob")
                .status("PENDING")
                .build();

        when(dataAccessApiClient.getApplications()).thenReturn(List.of(app1, app2));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.getApplicationById("999")
        );

        assertEquals("Application not found: 999", exception.getMessage());
    }
}