package com.example.demo.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcceptedStringsValidator implements ConstraintValidator<AcceptedStrings, String> {
    private List<String> values;

    @Override
    public void initialize(AcceptedStrings constraintAnnotation) {
        this.values = new ArrayList<>();
        this.values.addAll(Arrays.asList(constraintAnnotation.value()));
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s == null || this.values.contains(s);
    }
}
