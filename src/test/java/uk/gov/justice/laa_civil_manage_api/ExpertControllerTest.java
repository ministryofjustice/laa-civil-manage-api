package uk.gov.justice.laa_civil_manage_api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa_civil_manage_api.config.SecurityConfig;
import uk.gov.justice.laa_civil_manage_api.controllers.ExpertController;
import uk.gov.justice.laa_civil_manage_api.services.ExpertService;

@WebMvcTest(ExpertController.class)
@Import(SecurityConfig.class)
public class ExpertControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @MockitoBean private ExpertService expertService;

  @Test
  void shouldReturnExpertTypesForTheRequestedMatterType() throws Exception {
    when(expertService.getExpertTypeDescriptions("KMAAA"))
        .thenReturn(List.of("Psychologist", "Interpreter"));

    mockMvc
        .perform(get("/expertTypes").param("matterType", "KMAAA").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0]").value("Psychologist"))
        .andExpect(jsonPath("$[1]").value("Interpreter"));
  }

  @Test
  void shouldDefaultToKpblwWhenNoMatterTypeIsSupplied() throws Exception {
    when(expertService.getExpertTypeDescriptions("KPBLW")).thenReturn(List.of("Psychologist"));

    mockMvc
        .perform(get("/expertTypes").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("Psychologist"));

    verify(expertService).getExpertTypeDescriptions("KPBLW");
  }

  @Test
  void shouldReturnEmptyArrayWhenTheMatterTypeHasNoExpertTypes() throws Exception {
    when(expertService.getExpertTypeDescriptions(any())).thenReturn(List.of());

    mockMvc
        .perform(get("/expertTypes").param("matterType", "NOPE").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void shouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/expertTypes")).andExpect(status().isUnauthorized());
  }
}
