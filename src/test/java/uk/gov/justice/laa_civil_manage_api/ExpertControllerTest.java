package uk.gov.justice.laa_civil_manage_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.laa_civil_manage_api.config.LaaCivilManageApiConfig;
import uk.gov.justice.laa_civil_manage_api.config.SecurityConfig;
import uk.gov.justice.laa_civil_manage_api.controllers.ExpertController;

@EnableConfigurationProperties(LaaCivilManageApiConfig.class)
@WebMvcTest(ExpertController.class)
@Import(SecurityConfig.class)
public class ExpertControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  private static final ObjectMapper mapper =
      JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).build();

  @Test
  void shouldReturnExpertTypes() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/expertTypes").with(jwt())).andExpect(status().isOk()).andReturn();

    List<String> expertTypes =
        mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

    assertEquals(112, expertTypes.size());
    assertEquals("A & E Consultant", expertTypes.getFirst());
  }
}
