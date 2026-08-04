package uk.gov.justice.laa_civil_manage_api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExpertTypesIntegrationTest {

  private static final ParameterizedTypeReference<List<String>> STRING_LIST =
      new ParameterizedTypeReference<>() {};

  @RegisterExtension
  static WireMockExtension legalFramework =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void legalFrameworkProperties(DynamicPropertyRegistry registry) {
    registry.add("laa-civil-manage-api.legal-framework.base-url", legalFramework::baseUrl);
  }

  @LocalServerPort private int port;

  @MockitoBean private JwtDecoder jwtDecoder;

  @BeforeEach
  void setUp() {
    Jwt mockJwt =
        Jwt.withTokenValue("test-token").header("alg", "none").claim("sub", "test-user").build();
    when(jwtDecoder.decode("test-token")).thenReturn(mockJwt);
  }

  private RestClient authenticatedClient() {
    return RestClient.builder().defaultHeader("Authorization", "Bearer test-token").build();
  }

  private ResponseEntity<List<String>> getExpertTypes(String query) {
    return authenticatedClient()
        .get()
        .uri("http://localhost:" + port + "/expertTypes" + query)
        .retrieve()
        .onStatus(HttpStatusCode::isError, (_, _) -> {})
        .toEntity(STRING_LIST);
  }

  private HttpStatusCode getExpertTypesStatus(String query) {
    return authenticatedClient()
        .get()
        .uri("http://localhost:" + port + "/expertTypes" + query)
        .retrieve()
        .onStatus(HttpStatusCode::isError, (_, _) -> {})
        .toBodilessEntity()
        .getStatusCode();
  }

  @Test
  void returnsTheDescriptionsFromTheLegalFrameworkApi() {
    legalFramework.stubFor(
        get(urlEqualTo("/expert_types/KPBLW"))
            .willReturn(
                okJson(
                    """
                    [
                      {"code": "child_psychologist", "description": "Child Psychologist"},
                      {"code": "interpreter", "description": "Interpreter"}
                    ]
                    """)));

    ResponseEntity<List<String>> response = getExpertTypes("");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of("Child Psychologist", "Interpreter"), response.getBody());
  }

  @Test
  void defaultsToKpblwWhenNoMatterTypeIsSupplied() {
    legalFramework.stubFor(get(urlPathEqualTo("/expert_types/KPBLW")).willReturn(okJson("[]")));

    getExpertTypes("");

    legalFramework.verify(getRequestedFor(urlEqualTo("/expert_types/KPBLW")));
  }

  @Test
  void passesAMatterTypeSuppliedByTheFrontEndThrough() {
    legalFramework.stubFor(
        get(urlEqualTo("/expert_types/KMAAA"))
            .willReturn(okJson("[{\"code\": \"vet\", \"description\": \"Vet\"}]")));

    ResponseEntity<List<String>> response = getExpertTypes("?matterType=KMAAA");

    assertEquals(List.of("Vet"), response.getBody());
    legalFramework.verify(getRequestedFor(urlEqualTo("/expert_types/KMAAA")));
  }

  @Test
  void returnsAnEmptyListWhenTheMatterTypeHasNoExpertTypes() {
    legalFramework.stubFor(get(urlEqualTo("/expert_types/KPBLW")).willReturn(okJson("[]")));

    ResponseEntity<List<String>> response = getExpertTypes("");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(List.of(), response.getBody());
  }

  @Test
  void surfacesA404FromTheLegalFrameworkApiRatherThanPretendingThereAreNoExpertTypes() {
    legalFramework.stubFor(
        get(urlEqualTo("/expert_types/KPBLW")).willReturn(aResponse().withStatus(404)));

    assertEquals(HttpStatus.NOT_FOUND, getExpertTypesStatus(""));
  }

  @Test
  void translatesA500FromTheLegalFrameworkApiInto502() {
    legalFramework.stubFor(
        get(urlEqualTo("/expert_types/KPBLW")).willReturn(aResponse().withStatus(500)));

    assertEquals(HttpStatus.BAD_GATEWAY, getExpertTypesStatus(""));
  }

  @Test
  void reportsTheLegalFrameworkApiAsUpWhenItsOwnChecksPass() {
    legalFramework.stubFor(
        get(urlEqualTo("/status")).willReturn(okJson("{\"checks\":{\"database\":true}}")));

    Map<String, Object> health = health("/actuator/health");

    assertEquals("UP", health.get("status"));
    assertEquals("UP", component(health, "legalFramework").get("status"));
  }

  @Test
  void reportsTheLegalFrameworkApiAsDownWhenItsOwnChecksFail() {
    legalFramework.stubFor(
        get(urlEqualTo("/status")).willReturn(okJson("{\"checks\":{\"database\":false}}")));

    Map<String, Object> health = health("/actuator/health");

    assertEquals("DOWN", health.get("status"));
    assertEquals("DOWN", component(health, "legalFramework").get("status"));
  }

  @Test
  void reportsTheLegalFrameworkApiAsDownWhenItCannotBeReached() {
    legalFramework.stubFor(get(urlEqualTo("/status")).willReturn(aResponse().withStatus(503)));

    assertEquals("DOWN", component(health("/actuator/health"), "legalFramework").get("status"));
  }

  @Test
  void keepsTheKubernetesProbesUpWhenTheLegalFrameworkApiIsDown() {
    legalFramework.stubFor(
        get(urlEqualTo("/status")).willReturn(okJson("{\"checks\":{\"database\":false}}")));

    assertEquals("DOWN", health("/actuator/health").get("status"));
    assertEquals("UP", health("/actuator/health/liveness").get("status"));
    assertEquals("UP", health("/actuator/health/readiness").get("status"));
  }

  @Test
  void exposesTheProbeGroupsWithoutAuthentication() {
    for (String path : List.of("/actuator/health/liveness", "/actuator/health/readiness")) {
      ResponseEntity<Void> response =
          RestClient.create()
              .get()
              .uri("http://localhost:" + port + path)
              .retrieve()
              .onStatus(HttpStatusCode::isError, (_, _) -> {})
              .toBodilessEntity();

      assertTrue(
          response.getStatusCode().is2xxSuccessful(),
          path + " returned " + response.getStatusCode());
    }
  }

  private Map<String, Object> health(String path) {
    ResponseEntity<Map<String, Object>> response =
        RestClient.create()
            .get()
            .uri("http://localhost:" + port + path)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (_, _) -> {})
            .toEntity(new ParameterizedTypeReference<>() {});

    assertNotNull(response.getBody(), "no health body for " + path);
    return response.getBody();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> component(Map<String, Object> health, String name) {
    Map<String, Object> components = (Map<String, Object>) health.get("components");
    assertNotNull(components, "health response had no components");
    Map<String, Object> component = (Map<String, Object>) components.get(name);
    assertNotNull(component, "health response had no " + name + " component");
    return component;
  }
}
