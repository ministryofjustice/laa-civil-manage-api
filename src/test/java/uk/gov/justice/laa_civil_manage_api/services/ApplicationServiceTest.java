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

        Application app1 = buildDummyApplication("1", "Janet");
        Application app2 = buildDummyApplication("2", "Bob");
        List<Application> expectedApplications = List.of(app1, app2);

        when(dataAccessApiClient.getApplications()).thenReturn(expectedApplications);

        List<Application> actualApplications = applicationService.getApplications();

        assertEquals(2, actualApplications.size());
        assertEquals(expectedApplications, actualApplications);
        verify(dataAccessApiClient, times(1)).getApplications();
    }

    @Test
    void shouldReturnApplicationWhenIdExists() {

        Application app1 = buildDummyApplication("1", "Janet");
        Application app2 = buildDummyApplication("2", "Bob");

        when(dataAccessApiClient.getApplications()).thenReturn(List.of(app1, app2));

        Application result = applicationService.getApplicationById("2");

        assertNotNull(result);
        assertEquals("2", result.applicationId());
        assertEquals("Bob", result.clientFirstName());
    }

    @Test
    void shouldThrowExceptionWhenIdDoesNotExist() {

        Application app1 = buildDummyApplication("1", "Janet");
        Application app2 = buildDummyApplication("2", "Bob");

        when(dataAccessApiClient.getApplications()).thenReturn(List.of(app1, app2));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.getApplicationById("999")
        );

        assertEquals("Application not found: 999", exception.getMessage());
    }

    private Application buildDummyApplication(String id, String firstName) {
        return new Application(
                id,
                "PENDING",
                null,
                null,
                false,
                "FAMILY",
                null,
                null,
                false,
                firstName,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }
}