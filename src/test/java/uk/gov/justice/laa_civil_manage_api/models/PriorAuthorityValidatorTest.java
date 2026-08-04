package uk.gov.justice.laa_civil_manage_api.models;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PriorAuthorityValidatorTest {

  private static ValidatorFactory factory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    factory.close();
  }

  private static PriorAuthority.PriorAuthorityBuilder validExpert() {
    return PriorAuthority.builder()
        .applicationId(UUID.randomUUID())
        .priorAuthorityType(PriorAuthorityType.EXPERT)
        .expertType("Psychologist")
        .expertFullName("John Doe")
        .uploadedDocuments(List.of(UploadedDocument.builder().fileName("report.pdf").build()))
        .billingType(BillingType.FIXED_RATE)
        .totalAmount(new BigDecimal("125.00"))
        .justification("Specialist evidence is required.");
  }

  private static PriorAuthority.PriorAuthorityBuilder validCounsel() {
    return PriorAuthority.builder()
        .applicationId(UUID.randomUUID())
        .priorAuthorityType(PriorAuthorityType.COUNSEL)
        .counselType(CounselType.KINGS_COUNSEL_ALONE)
        .uploadedDocuments(List.of(UploadedDocument.builder().fileName("instructions.pdf").build()))
        .justification("Counsel is required to advise on complex points of law.");
  }

  private static Set<String> invalidFields(PriorAuthority priorAuthority) {
    return validator.validate(priorAuthority).stream()
        .map(violation -> violation.getPropertyPath().toString())
        .collect(toSet());
  }

  @Test
  void validFixedRateExpertHasNoViolations() {
    assertThat(validator.validate(validExpert().build())).isEmpty();
  }

  @Test
  void validHourlyExpertHasNoViolations() {
    PriorAuthority priorAuthority =
        validExpert()
            .billingType(BillingType.HOURLY)
            .hourlyRate(new BigDecimal("50.00"))
            .timeHours(2)
            .timeMinutes(30)
            .build();

    assertThat(validator.validate(priorAuthority)).isEmpty();
  }

  @Test
  void validCounselHasNoViolations() {
    assertThat(validator.validate(validCounsel().build())).isEmpty();
  }

  @Test
  void expertRequiresExpertType() {
    assertThat(invalidFields(validExpert().expertType(null).build())).contains("expertType");
  }

  @Test
  void expertRequiresExpertTypeToNotBeBlank() {
    assertThat(invalidFields(validExpert().expertType("  ").build())).contains("expertType");
  }

  @Test
  void expertRequiresExpertFullName() {
    assertThat(invalidFields(validExpert().expertFullName(null).build()))
        .contains("expertFullName");
  }

  @Test
  void expertRequiresBillingType() {
    Set<String> fields = invalidFields(validExpert().billingType(null).build());

    assertThat(fields).contains("billingType");
    assertThat(fields).doesNotContain("hourlyRate", "timeHours", "timeMinutes");
  }

  @Test
  void expertRequiresTotalAmount() {
    assertThat(invalidFields(validExpert().totalAmount(null).build())).contains("totalAmount");
  }

  @Test
  void expertRequiresAtLeastOneUploadedDocument() {
    assertThat(invalidFields(validExpert().uploadedDocuments(null).build()))
        .contains("uploadedDocuments");
    assertThat(invalidFields(validExpert().uploadedDocuments(List.of()).build()))
        .contains("uploadedDocuments");
  }

  @Test
  void hourlyExpertRequiresTimeAndRateFields() {
    PriorAuthority priorAuthority = validExpert().billingType(BillingType.HOURLY).build();

    assertThat(invalidFields(priorAuthority)).contains("hourlyRate", "timeHours", "timeMinutes");
  }

  @Test
  void fixedRateExpertDoesNotRequireTimeAndRateFields() {
    PriorAuthority priorAuthority = validExpert().billingType(BillingType.FIXED_RATE).build();

    assertThat(invalidFields(priorAuthority))
        .doesNotContain("hourlyRate", "timeHours", "timeMinutes");
  }

  @Test
  void counselRequiresCounselType() {
    assertThat(invalidFields(validCounsel().counselType(null).build())).contains("counselType");
  }

  @Test
  void counselRequiresAtLeastOneUploadedDocument() {
    assertThat(invalidFields(validCounsel().uploadedDocuments(List.of()).build()))
        .contains("uploadedDocuments");
  }

  @Test
  void counselDoesNotRequireBillingTypeOrTotalAmount() {
    PriorAuthority priorAuthority = validCounsel().billingType(null).totalAmount(null).build();

    assertThat(invalidFields(priorAuthority)).doesNotContain("billingType", "totalAmount");
  }

  @Test
  void disbursementHasNoConditionalRules() {
    PriorAuthority priorAuthority =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .priorAuthorityType(PriorAuthorityType.DISBURSEMENT)
            .justification("Disbursement is required.")
            .build();

    assertThat(validator.validate(priorAuthority)).isEmpty();
  }

  @Test
  void nullPriorAuthorityTypeSkipsConditionalValidation() {
    PriorAuthority priorAuthority =
        PriorAuthority.builder()
            .applicationId(UUID.randomUUID())
            .justification("Type to be provided.")
            .build();

    // Only the field-level @NotNull on priorAuthorityType fires; no conditional rules run.
    assertThat(invalidFields(priorAuthority)).containsExactly("priorAuthorityType");
  }

  @Test
  void multipleMissingExpertFieldsAreAllReported() {
    PriorAuthority priorAuthority =
        validExpert()
            .expertType(null)
            .expertFullName(null)
            .totalAmount(null)
            .uploadedDocuments(List.of())
            .build();

    assertThat(invalidFields(priorAuthority))
        .contains("expertType", "expertFullName", "totalAmount", "uploadedDocuments");
  }
}
