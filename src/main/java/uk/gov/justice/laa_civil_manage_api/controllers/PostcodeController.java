package uk.gov.justice.laa_civil_manage_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.services.PostcodeLookupService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PostcodeController {

  private final PostcodeLookupService postcodeLookupService;

  @Operation(
      summary = "Check whether postcode is in London",
      description =
          "Looks up the postcode using OS Places and returns whether the local custodian code is one of the configured London custodian codes.")
  @GetMapping("/postcodes/london")
  public PostcodeLondonResponse isPostcodeInLondon(
      @Parameter(description = "UK postcode to check, e.g. SW1A 1AA") @RequestParam @NotBlank
          String postcode) {
    return new PostcodeLondonResponse(postcodeLookupService.isInLondon(postcode));
  }

  public record PostcodeLondonResponse(boolean inLondon) {}
}
