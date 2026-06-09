package uk.gov.justice.laa_civil_manage_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraft;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityDraftSummary;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityDraftService;
import uk.gov.justice.laa_civil_manage_api.services.PriorAuthorityService;

@Tag(
    name = "Prior Authority",
    description =
        "Submit prior-authority requests to the Legal Aid Agency, and save/resume in-progress drafts")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/prior-authority")
@RequiredArgsConstructor
public class PriorAuthorityController {

  private final PriorAuthorityService priorAuthorityService;
  private final PriorAuthorityDraftService draftService;

  @Operation(
      summary = "Submit a prior-authority request",
      description =
          "Accepts a prior-authority request from the LAA Civil Manage frontend and forwards it "
              + "to the Access Data Store.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Request accepted. Location header contains the submission URL.",
        content =
            @Content(schema = @Schema(implementation = PriorAuthorityApplicationResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
  })
  @PostMapping
  public ResponseEntity<PriorAuthorityApplicationResponse> submit(
      @Valid @RequestBody PriorAuthority priorAuthority) {
    PriorAuthorityApplicationResponse response = priorAuthorityService.submit(priorAuthority);
    URI location = URI.create("/prior-authority/" + response.submissionId());
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Create a prior-authority draft",
      description = "Saves the supplied form as a new draft.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Draft created. Location header contains the draft URL."),
    @ApiResponse(responseCode = "400", description = "applicationId missing.", content = @Content)
  })
  @PostMapping("/drafts")
  public ResponseEntity<DraftIdResponse> createDraft(
      @Valid @RequestBody PriorAuthorityDraft draft) {
    UUID draftId = draftService.create(draft);
    URI location = URI.create("/prior-authority/drafts/" + draftId);
    return ResponseEntity.created(location).body(new DraftIdResponse(draftId));
  }

  @Operation(summary = "Update an existing prior-authority draft")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Draft updated."),
    @ApiResponse(responseCode = "404", description = "Draft not found.", content = @Content)
  })
  @PutMapping("/drafts/{draftId}")
  @ResponseStatus(HttpStatus.OK)
  public void updateDraft(
      @Parameter(description = "ID of the draft to update.") @PathVariable UUID draftId,
      @Valid @RequestBody PriorAuthorityDraft draft) {
    draftService.update(draftId, draft);
  }

  @Operation(summary = "Retrieve an existing prior-authority draft")
  @GetMapping("/drafts/{draftId}")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Draft returned."),
    @ApiResponse(responseCode = "404", description = "Draft not found.", content = @Content)
  })
  public ResponseEntity<PriorAuthorityDraftSummary> getDraft(
      @Parameter(description = "ID of the draft to return.") @PathVariable UUID draftId) {
    return draftService
        .get(draftId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "List prior-authority drafts for an Application",
      description =
          "Returns all of the current user's prior-authority drafts. "
              + "Use applicationId to narrow the list to a specific application.")
  @GetMapping("/drafts")
  public List<PriorAuthorityDraftSummary> listDrafts(
      @Parameter(description = "Optional parent application ID filter.")
          @RequestParam(required = false)
          UUID applicationId) {
    return draftService.list(applicationId);
  }

  @Operation(
      summary = "Delete a prior-authority draft",
      description = "Permanently removes the draft")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Draft deleted (or did not exist).")
  })
  @DeleteMapping("/drafts/{draftId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDraft(
      @Parameter(description = "ID of the draft to delete.") @PathVariable UUID draftId) {
    draftService.delete(draftId);
  }

  public record DraftIdResponse(UUID draftId) {}
}
