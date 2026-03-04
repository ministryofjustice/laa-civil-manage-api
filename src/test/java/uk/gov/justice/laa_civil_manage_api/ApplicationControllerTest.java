package uk.gov.justice.laa_civil_manage_api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import uk.gov.justice.controllers.ApplicationController;
import uk.gov.justice.laa_civil_manage_api.models.Application;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config()
                .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> mapper));
    }

    @Test
    void shouldReturnApplicationsArray() {
        Application[] applications = given()
                .when()
                .get("/applications")
                .then()
                .statusCode(200)
                .extract()
                .as(Application[].class);

        Application firstApp = applications[0];
        assertEquals("Jane", firstApp.getClientFirstName());
    }
}