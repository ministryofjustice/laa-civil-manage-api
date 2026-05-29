package uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.controllers;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.mockaccessdatastore.services.MockDraftStore;
import uk.gov.justice.laa_civil_manage_api.models.Draft;
import uk.gov.justice.laa_civil_manage_api.models.DraftCreatedResponse;
import uk.gov.justice.laa_civil_manage_api.models.DraftSummary;

@Tag(
        name = "Mock Access Data Store — Drafts",
        description = "Mock of the Access Data Store endpoints that store form drafts."
)
@RestController
@RequestMapping("/mock-access-data-store/drafts")
@RequiredArgsConstructor
public class MockDraftController {

    private final MockDraftStore store;

    @Operation(summary = "Create a draft in the Access Data Store")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Draft created.",
                    content = @Content(schema = @Schema(implementation = DraftCreatedResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DraftCreatedResponse> create(@Valid @RequestBody Draft draft) {
        DraftCreatedResponse response = store.create(draft);
        URI location = URI.create("/mock-access-data-store/drafts/" + response.draftId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Update a draft in the Access Data Store")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Draft updated."),
            @ApiResponse(responseCode = "404", description = "Draft not found.", content = @Content)
    })
    @PutMapping("/{draftId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(
            @Parameter(description = "ID of the draft to update.") @PathVariable UUID draftId,
            @Valid @RequestBody Draft draft
    ) {
        store.update(draftId, draft);
    }

    @GetMapping("/{draftId}")
    public ResponseEntity<DraftSummary> get(
            @Parameter(description = "ID of the draft to query.") @PathVariable UUID draftId
    ) {
        return store.get(draftId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List drafts matching the given keys")
    @GetMapping
    public List<DraftSummary> list(
            @Parameter(required = true) @RequestParam String sourceSystem,
            @Parameter(required = true) @RequestParam String userId,
            @RequestParam(required = false) String draftType,
            @RequestParam(required = false) UUID applicationId
    ) {
        return store.list(sourceSystem, userId, draftType, applicationId);
    }

    @Operation(summary = "Delete a draft from the Access Data Store")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Draft deleted (or did not exist).")
    })
    @DeleteMapping("/{draftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID draftId) {
        store.delete(draftId);
    }
}
