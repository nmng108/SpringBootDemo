package com.example.demo.Model.DTO.Request;

import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PersonSearchDTO {
    private String id;

    @Length(min = 5, max = 45, message = "Invalid name length")
    private String name;

    private String birthDate;

    private String height; // unit: cm

    private String weight;

    @Length(min = 1, max = 70, message = "Invalid address length")
    private String address;

    @Length(min = 1, max = 20, message = "Invalid identity length")
    private String identity;

    @AcceptedStrings({"and", "or"})
    private String mode;

    @Pattern(regexp = "[a-zA-Z]{1,10}", message = "Invalid sorted field names")
    private String sortBy;

    @AcceptedStrings({"asc", "desc"})
    private String order;

    private static final String[] OPERATORS = {"eq", "lt", "le", "gt", "ge", "like"};

    public String getFormattedId() {
        return "id " + PersonSearchDTO.formatParamValue(id);
    }

    public String getFormattedName() {
        return "name " + PersonSearchDTO.formatParamValue(name);
    }

    public String getFormattedBirthDate() {
        return "birthdate " + PersonSearchDTO.formatParamValue(birthDate);
    }

    public String getFormattedHeight() {
        return "height " + PersonSearchDTO.formatParamValue(height);
    }

    public String getFormattedWeight() {
        return "weight " + PersonSearchDTO.formatParamValue(weight);
    }

    public String getFormattedAddress() {
        return "address" + PersonSearchDTO.formatParamValue(address);
    }

    public String getFormattedIdentity() {
        return "identity " + PersonSearchDTO.formatParamValue(identity);
    }

    private static String formatParamValue(String value) {
        if (value == null) return null;

        for (String operator : PersonSearchDTO.OPERATORS) {
            if (value.startsWith(operator + " ")) return value;
        }

        return "= " + value;
    }
}
