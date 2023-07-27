package com.example.demo.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AcceptedStringsValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
//@NotNull(message = "Value cannot be null")
@ReportAsSingleViolation
public @interface AcceptedStrings {
    String[] value();
    String message() default "Value is not allowed";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
