package uk.gov.justice.laa_civil_manage_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityService;

@Tag(
        name = "Prior Authority",
        description = "Submit requests for prior-authority to the Legal Aid Agency. "
                + "Requests are forwarded to the Access Data Store"
)
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/prior-authority-requests")
@RequiredArgsConstructor
public class PriorAuthorityController {

    private final PriorAuthorityService priorAuthorityService;

    @Operation(
            summary = "Submit a prior-authority request",
            description = "Accepts a prior-authority request from the LAA Civil Manage frontend and forwards it "
                    + "to the Access Data Store. The applicationId in the payload identifies which application "
                    + "the request relates to. Returns the submission ID assigned by the Access Data Store."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Request accepted. Location header contains the submission URL.",
                    content = @Content(schema = @Schema(implementation = PriorAuthorityApplicationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (missing required field, billing-type / field mismatch, etc.).",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<PriorAuthorityApplicationResponse> submit(
            @Valid @RequestBody PriorAuthority priorAuthority
    ) {
        PriorAuthorityApplicationResponse response = priorAuthorityService.submit(priorAuthority);
        URI location = URI.create("/prior-authority-requests/" + response.submissionId());
        return ResponseEntity.created(location).body(response);
    }
}
