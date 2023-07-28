package com.example.demo.Model.DTO.Request;

import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

//    private final PersonSearchDTO personSearchDTO = this;
    @Pattern(regexp = "[a-zA-Z]{1,10}", message = "Invalid sorted field names")
    private String sortBy;

    @AcceptedStrings({"asc", "desc"})
    private String order;

    private static final String[] OPERATORS = {"eq", "lt", "le", "gt", "ge", "like"};

    public record FormattedCondition(String propertyName, String condition) {
        public FormattedCondition {
            Objects.requireNonNull(propertyName);
            Objects.requireNonNull(condition);
        }
    }

    public FormattedCondition getFormattedId() {
        return new FormattedCondition("id", PersonSearchDTO.formatParamValue(this.id));
    }

    public FormattedCondition getFormattedName() {
        return new FormattedCondition("name", PersonSearchDTO.formatParamValue(this.name));
    }

    public FormattedCondition getFormattedBirthDate() {
        return new FormattedCondition("birthDate", PersonSearchDTO.formatParamValue(this.birthDate));
    }

    public FormattedCondition getFormattedHeight() {
        return new FormattedCondition("height", PersonSearchDTO.formatParamValue(this.height));
    }

    public FormattedCondition getFormattedWeight() {
        return new FormattedCondition("weight", PersonSearchDTO.formatParamValue(this.weight));
    }

    public FormattedCondition getFormattedAddress() {
        return new FormattedCondition("address", PersonSearchDTO.formatParamValue(this.address));
    }

    public FormattedCondition getFormattedIdentity() {
        return new FormattedCondition("identity", PersonSearchDTO.formatParamValue(this.identity));
    }

    private static String formatParamValue(String value) {
        if (value == null) return null;

        for (String operator : PersonSearchDTO.OPERATORS) {
            if (value.startsWith(operator + " ")) return value;
        }

        return "= " + value;
    }

    public Map<String, String> getFormattedCriteria() {
        Map<String, String> stringCriteria = new HashMap<>();

        if (this.id != null) stringCriteria.put("id", this.getFormattedId().condition());
        if (this.identity != null) stringCriteria.put("identity", this.getFormattedIdentity().condition());
        if (this.name != null) stringCriteria.put("name", this.getFormattedName().condition());
        if (this.address != null) stringCriteria.put("address", this.getFormattedAddress().condition());
        if (this.birthDate != null) stringCriteria.put("birthDate", this.getFormattedBirthDate().condition());
        if (this.height != null) stringCriteria.put("height", this.getFormattedHeight().condition());
        if (this.weight != null) stringCriteria.put("weight", this.getFormattedWeight().condition());

        return stringCriteria;
    }

}
