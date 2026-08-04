package uk.gov.justice.laa_civil_manage_api.models;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class PriorAuthorityValidator implements ConstraintValidator<ValidPriorAuthority, PriorAuthority> {

  @Override
  public boolean isValid(PriorAuthority value, ConstraintValidatorContext context) {
    if (value == null || value.priorAuthorityType() == null) {
      // Nulls handled by field-level @NotNull constraints.
      return true;
    }

    context.disableDefaultConstraintViolation();
    boolean valid = true;

    switch (value.priorAuthorityType()) {
      case EXPERT -> {
        if (isBlank(value.expertType())) {
          valid = reject(context, "must not be blank", "expertType");
        }
        if (isBlank(value.expertFullName())) {
          valid = reject(context, "must not be blank", "expertFullName");
        }
        if (value.billingType() == null) {
          valid = reject(context, "must not be null", "billingType");
        }
        if (value.totalAmount() == null) {
          valid = reject(context, "must not be null", "totalAmount");
        }
        if (isEmpty(value.uploadedDocuments())) {
          valid = reject(context, "must not be empty", "uploadedDocuments");
        }
        if (value.billingType() == BillingType.HOURLY) {
          if (value.hourlyRate() == null) {
            valid = reject(context, "must not be null", "hourlyRate");
          }
          if (value.timeHours() == null) {
            valid = reject(context, "must not be null", "timeHours");
          }
          if (value.timeMinutes() == null) {
            valid = reject(context, "must not be null", "timeMinutes");
          }
        }
      }
      case COUNSEL -> {
        if (value.counselType() == null) {
          valid = reject(context, "must not be null", "counselType");
        }
        if (isEmpty(value.uploadedDocuments())) {
          valid = reject(context, "must not be empty", "uploadedDocuments");
        }
      }
      case DISBURSEMENT -> {
        // TODO Not yet implemented; no additional validation rules.
      }
    }

    return valid;
  }

  private static boolean reject(
      ConstraintValidatorContext context, String message, String property) {
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode(property)
        .addConstraintViolation();
    return false;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static boolean isEmpty(List<?> value) {
    return value == null || value.isEmpty();
  }
}
