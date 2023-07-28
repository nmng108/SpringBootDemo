package com.example.demo.Model.DTO.Request;

import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class PersonSearchDTO {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 5;
    private static final String[] OPERATORS = {"eq", "lt", "le", "gt", "ge", "like"};

    @Pattern(regexp = "([0-9]+)|([a-z]{2,5}\s[0-9]+)", message = "Invalid id") // may be more detail
    private String id;

    @Pattern(regexp = "[0-9a-zA-Z-]{5,45}", message = "Invalid name")
    private String name;

    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}", message = "Invalid name") // may be more detail
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

    @Pattern(regexp = "[0-9]+(,)?([0-9]+)?", message = "Invalid page")
    private String page;

    @Digits(integer = 2, fraction = 0)
    private Integer size;

    public Integer getSize() {
        return Objects.isNull(this.size) ? DEFAULT_SIZE : this.size;
    }

//    public Map<String, Integer> getPage() {
//        Map<String, Integer> result = new HashMap<>();
//        int from = 0;
//        Integer to = null;
//
//        if (page.matches("^[0-9]+$")) { // only 1 number exists
//            from = Integer.parseInt(page);
//        } else if (page.matches("^[0-9]+,([0-9]+)?$")) { // the 2nd number may exist or not
//            String[] splitPageValue = page.split(",");
//            from = Integer.parseInt(splitPageValue[0]);
//            // get all remaining records (start from the 'from' page) if the 2nd number isn't specified
//            to = splitPageValue.length == 1 ? Integer.MAX_VALUE : Integer.parseInt(splitPageValue[1]);
//        } else {
//            throw new InvalidRequestException("Invalid page");
//        }
//
//        result.put("from", from);
//        result.put("to", to);
//        return result;
//    }

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
