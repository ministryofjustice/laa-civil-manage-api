package uk.gov.justice.laa_civil_manage_api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    private String applicationId;
    private String status;
    private OffsetDateTime submittedAt;
    private OffsetDateTime lastUpdated;
    private boolean usedDelegatedFunctions;
    private String categoryOfLaw;
    private String matterType;
    private UUID assignedTo;
    private boolean autoGrant;
    private String clientFirstName;
    private String clientLastName;
    private LocalDate clientDateOfBirth;
    private String laaReference;
    private String officeCode;
    private String applicationType;
    private boolean isLead;
}