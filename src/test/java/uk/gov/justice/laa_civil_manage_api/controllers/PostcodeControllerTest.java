package uk.gov.justice.laa_civil_manage_api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.justice.laa_civil_manage_api.services.PostcodeLookupService;

@WebMvcTest(PostcodeController.class)
class PostcodeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PostcodeLookupService postcodeLookupService;

  @Test
  void returnsTrueWhenPostcodeIsInLondon() throws Exception {
    when(postcodeLookupService.isInLondon("SW1A 1AA")).thenReturn(true);

    mockMvc
        .perform(get("/postcodes/london").param("postcode", "SW1A 1AA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.inLondon").value(true));
  }

  @Test
  void returnsFalseWhenPostcodeIsNotInLondon() throws Exception {
    when(postcodeLookupService.isInLondon("LS1 1UR")).thenReturn(false);

    mockMvc
        .perform(get("/postcodes/london").param("postcode", "LS1 1UR"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.inLondon").value(false));
  }

  @Test
  void returns400WhenPostcodeIsBlank() throws Exception {
    mockMvc
        .perform(get("/postcodes/london").param("postcode", " "))
        .andExpect(status().isBadRequest());
  }
}
