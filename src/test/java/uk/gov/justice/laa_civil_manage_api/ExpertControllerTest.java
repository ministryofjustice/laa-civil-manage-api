package uk.gov.justice.laa_civil_manage_api;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.laa_civil_manage_api.controllers.ExpertController;

@WebMvcTest(ExpertController.class)
public class ExpertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .build();

    @Test
    void shouldReturnExpertTypes() throws Exception {
        MvcResult result = mockMvc.perform(get("/expertTypes"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> expertTypes = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(113, expertTypes.size());
        assertEquals("A & E Consultant", expertTypes.getFirst());
    }

}