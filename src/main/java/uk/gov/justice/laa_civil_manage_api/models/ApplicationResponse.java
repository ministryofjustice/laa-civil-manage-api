package uk.gov.justice.laa_civil_manage_api.models;
import java.util.List;

public record ApplicationResponse(
        List<Application> applications
) {}
