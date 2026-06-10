package uk.gov.justice.laa_civil_manage_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class AccessDataStoreErrorHandler {

  @ExceptionHandler(HttpClientErrorException.class)
  public ResponseEntity<ProblemDetail> handleClientError(HttpClientErrorException ex) {
    ProblemDetail body =
        ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(), "Access Data Store returned " + ex.getStatusCode().value());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity<ProblemDetail> handleServerError(HttpServerErrorException ex) {
    ProblemDetail body =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY, "Access Data Store returned " + ex.getStatusCode().value());
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
  }
}
