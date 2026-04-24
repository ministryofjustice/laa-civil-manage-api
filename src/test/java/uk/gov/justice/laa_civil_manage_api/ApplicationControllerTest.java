// package uk.gov.justice.laa_civil_manage_api;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import tools.jackson.core.type.TypeReference;
// import tools.jackson.databind.DeserializationFeature;
// import tools.jackson.databind.ObjectMapper;
// import tools.jackson.databind.json.JsonMapper;
// import uk.gov.justice.laa_civil_manage_api.controllers.ApplicationController;
// import uk.gov.justice.laa_civil_manage_api.models.Application;
// import uk.gov.justice.laa_civil_manage_api.services.ApplicationService;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(ApplicationController.class)
// public class ApplicationControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockitoBean
//     private ApplicationService applicationService;

//     private static final ObjectMapper mapper = JsonMapper.builder()
//             .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
//             .build();

//     @Test
//     void shouldReturnApplications() throws Exception {
//         MvcResult result = mockMvc.perform(get("/applications"))
//                 .andExpect(status().isOk())
//                 .andReturn();

//         List<Application> applications = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>(){});

//         assertEquals(5, applications.size());
//         assertEquals("Ali", applications.getFirst().getClientFirstName());
//     }

// }