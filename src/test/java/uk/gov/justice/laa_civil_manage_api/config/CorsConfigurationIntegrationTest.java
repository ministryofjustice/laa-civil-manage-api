package uk.gov.justice.laa_civil_manage_api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import uk.gov.justice.laa_civil_manage_api.controllers.ExpertTypeController;
import uk.gov.justice.laa_civil_manage_api.services.ExpertTypeService;

/**
 * Verifies that CORS is restricted to the configured origin allowlist rather than a wildcard, and
 * that it is enforced centrally (i.e. applies to a controller with no CORS annotations).
 */
@WebMvcTest(ExpertTypeController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties =
        "laa-civil-manage-api.cors.allowed-origins=https://allowed-frontend.example.com, https://second-frontend.example.com, ")
class CorsConfigurationIntegrationTest {

  private static final String ALLOWED_ORIGIN = "https://allowed-frontend.example.com";
  private static final String DISALLOWED_ORIGIN = "https://not-allowed.example.com";

  @Autowired private MockMvc mockMvc;

  @Autowired private CorsConfigurationSource corsConfigurationSource;

  @MockitoBean private JwtDecoder jwtDecoder;

  @MockitoBean private ExpertTypeService expertTypeService;

  private CorsConfiguration configuration() {
    return corsConfigurationSource.getCorsConfiguration(
        new MockHttpServletRequest("GET", "/expertTypes"));
  }

  @Test
  void allowsPreflightFromAllowlistedOrigin() throws Exception {
    mockMvc
        .perform(
            options("/expertTypes")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
  }

  @Test
  void rejectsPreflightFromNonAllowlistedOrigin() throws Exception {
    mockMvc
        .perform(
            options("/expertTypes")
                .header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void doesNotReturnCorsHeadersForNonAllowlistedOriginOnActualRequest() throws Exception {
    mockMvc
        .perform(get("/expertTypes").header(HttpHeaders.ORIGIN, DISALLOWED_ORIGIN).with(jwt()))
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void returnsAllowOriginHeaderForAllowlistedOriginOnActualRequest() throws Exception {
    mockMvc
        .perform(get("/expertTypes").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN).with(jwt()))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
  }

  @Test
  void allowsAnyRequestHeaderSoApmAndTracingToolsDoNotBreakPreflight() throws Exception {
    mockMvc
        .perform(
            options("/expertTypes")
                .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Amzn-Trace-Id, traceparent"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));

    assertThat(configuration().getAllowedHeaders()).containsExactly("*");
  }

  @Test
  void exposesLocationHeaderSoFrontendCanReadItFrom201Responses() {
    assertThat(configuration().getExposedHeaders()).contains("Location", "X-Correlation-ID");
  }
}
