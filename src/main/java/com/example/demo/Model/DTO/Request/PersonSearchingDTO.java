package com.example.demo.Model.DTO.Request;

import lombok.Data;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Data
public class PersonSearchingDTO {
    private String id;

    @Length(min = 5, max = 45, message = "Invalid name length")
    private String name;

    private String birthDate;

    private String height; // unit: cm

    private String weight;

    @Length(min = 5, max = 70, message = "Invalid address's character sequence length")
    private String address;

    @Length(min = 5, max = 15, message = "Invalid identity length")
    private String identity;

    private static final String[] OPERATORS = {"eq", "lt", "le", "gt", "ge", "like"};

    public String getFormattedId() {
        return "id " + PersonSearchingDTO.formatParamValue(id);
    }

    public String getFormattedName() {
        return "name " + PersonSearchingDTO.formatParamValue(name);
    }

    public String getFormattedBirthDate() {
        return "birthdate " + PersonSearchingDTO.formatParamValue(birthDate);
    }

    public String getFormattedHeight() {
        return "height " + PersonSearchingDTO.formatParamValue(height);
    }

    public String getFormattedWeight() {
        return "weight " + PersonSearchingDTO.formatParamValue(weight);
    }

    public String getFormattedAddress() {
        return "address" + PersonSearchingDTO.formatParamValue(address);
    }

    public String getFormattedIdentity() {
        return "identity " + PersonSearchingDTO.formatParamValue(identity);
    }

    private static String formatParamValue(String value) {
        if (value == null) return null;

        boolean notContainsOperator = true;

        for (String operator : PersonSearchingDTO.OPERATORS) {
            if (value.startsWith(operator + " ")) notContainsOperator = false;
        }

        if (notContainsOperator) return "= " + value;
        return value;
    }
}
