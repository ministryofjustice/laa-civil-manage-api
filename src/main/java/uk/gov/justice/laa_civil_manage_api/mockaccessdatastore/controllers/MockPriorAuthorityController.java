package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.models.PriorAuthoritySubmission;
import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.services.MockPriorAuthorityStore;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;

@Tag(
        name = "Mock Access Data Store — Prior Authority",
        description = "Mock of the Access Data Store endpoint that accepts prior-authority submissions."
)
@RestController
@RequestMapping("/mock-access-data-store/applications/{applicationId}/prior-authorities")
@RequiredArgsConstructor
public class MockPriorAuthorityController {

    private final MockPriorAuthorityStore store;

    @Operation(
            summary = "Submit a prior-authority request against an application",
            description = "Accepts a prior-authority submission for the given application. "
                    + "Repeated submissions for the same applicationId return the same submission ID (idempotent)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Submission accepted by the Access Data Store.",
                    content = @Content(schema = @Schema(implementation = PriorAuthorityApplicationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed on the submission body.",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<PriorAuthorityApplicationResponse> submit(
            @Parameter(description = "ID of the application this prior-authority request is for.")
            @PathVariable UUID applicationId,
            @Valid @RequestBody PriorAuthoritySubmission submission
    ) {
        PriorAuthorityApplicationResponse response = store.submit(applicationId, submission);
        URI location = URI.create(
                "/mock-access-data-store/applications/" + applicationId
                        + "/prior-authorities/" + response.submissionId()
        );
        return ResponseEntity.created(location).body(response);
    }
}
