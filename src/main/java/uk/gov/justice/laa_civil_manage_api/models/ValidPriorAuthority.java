package uk.gov.justice.laa_civil_manage_api.models;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriorAuthorityValidator.class)
public @interface ValidPriorAuthority {

  String message() default "Invalid prior authority request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
