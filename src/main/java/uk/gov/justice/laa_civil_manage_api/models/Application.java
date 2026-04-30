package uk.gov.justice.laa_civil_manage_api.models;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Application(
        String applicationId,
        String status,
        OffsetDateTime submittedAt,
        OffsetDateTime lastUpdated,
        Boolean usedDelegatedFunctions,
        String categoryOfLaw,
        String matterType,
        UUID assignedTo,
        Boolean autoGrant,
        String clientFirstName,
        String clientLastName,
        LocalDate clientDateOfBirth,
        String laaReference,
        String officeCode,
        String applicationType,
        Boolean isLead
) {}
