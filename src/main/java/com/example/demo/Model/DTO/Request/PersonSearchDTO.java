package com.example.demo.Model.DTO.Request;

import com.example.demo.Exception.InvalidRequestException;
import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class PersonSearchDTO {
    public static final String[] OPERATORS = {"eq", "lt", "le", "gt", "ge", "like"};

    public static final String AND_SEARCH = "and";
    public static final String OR_SEARCH = "or";

    public static final String ASCENDING = "asc";
    public static final String DESCENDING = "desc";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 5;

//    @Pattern(regexp = "([0-9]+)|([a-z]{2,5} [0-9]+)", message = "Invalid id") // may be more detail
    private String id;

    @Pattern(regexp = "[0-9a-zA-Z-]{5,45}", message = "Invalid name")
    private String name;

    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}", message = "Invalid birthDate") // may be more detail
    private String birthDate;

//    @Pattern(regexp = "([0-9]+)|([a-z]{2,5}\s[0-9]+)", message = "Invalid height value") // may be more detail
    private String height; // unit: cm

//    @Pattern(regexp = "([0-9]+)|([a-z]{2,5}\s[0-9]+)", message = "Invalid weight value") // may be more detail
    private String weight;

    @Length(min = 1, max = 70, message = "Invalid address length")
    private String address;

    @Length(min = 1, max = 20, message = "Invalid identity length")
    private String identity;

    @AcceptedStrings(value = {AND_SEARCH, OR_SEARCH}, message = "\"mode\" is not an accepted value")
    private String mode;

    @Pattern(regexp = "[a-zA-Z]{1,10}", message = "Invalid sorted field names")
    private String sortBy;

    @AcceptedStrings(value = {ASCENDING, DESCENDING}, message = "\"order\" is not an accepted value")
    private String order;

    @Pattern(regexp = "[0-9]+(,)?([0-9]+)?", message = "Invalid page")
    private String page;

    @Digits(integer = 2, fraction = 0)
    private Integer size;

    public Integer getSize() {
        return Objects.isNull(this.size) ? DEFAULT_SIZE : this.size;
    }
}
