package uk.gov.justice.laa_civil_manage_api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

class RestClientErrorHandlerTest {

  private final RestClientErrorHandler handler = new RestClientErrorHandler();

  @Test
  void forwards404FromAccessDataStoreAs404() {
    HttpClientErrorException ex =
        HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);

    ResponseEntity<ProblemDetail> response = handler.handleClientError(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatus());
  }

  @Test
  void forwards400FromAccessDataStoreAs400() {
    HttpClientErrorException ex =
        HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);

    ResponseEntity<ProblemDetail> response = handler.handleClientError(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void translates5xxFromAccessDataStoreInto502BadGateway() {
    HttpServerErrorException ex =
        HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null);

    ResponseEntity<ProblemDetail> response = handler.handleServerError(ex);

    assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(502, response.getBody().getStatus());
  }
}
