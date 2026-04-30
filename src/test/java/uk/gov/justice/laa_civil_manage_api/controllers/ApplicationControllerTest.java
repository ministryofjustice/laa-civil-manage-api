package uk.gov.justice.laa_civil_manage_api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa_civil_manage_api.models.Application;
import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnApplications() throws Exception {
        when(applicationService.getApplications()).thenReturn(List.of(
                new Application("1", "PENDING", null, null, false, null, null, null, false, "Ali", "Fletcher", null, null, null, null, false)
        ));

        MvcResult result = mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andReturn();

        List<Application> applications = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(1, applications.size());
        assertEquals("Ali", applications.getFirst().clientFirstName());
    }
}
