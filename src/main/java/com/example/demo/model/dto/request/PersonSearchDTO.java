package com.example.demo.model.dto.request;

import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]+", message = "Invalid id") // may be more detail
    private String id;

    @Pattern(regexp = "[0-9a-zA-Z ]{1,45}", message = "Invalid name")
    private String name;

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]{4}-[0-9]{2}-[0-9]{2}", message = "Invalid birthDate") // may be more detail
    private String birthDate;

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9,.]{1,6}", message = "Invalid id") // may be more detail
    private String height; // unit: cm

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9,.]{1,6}", message = "Invalid id") // may be more detail
    private String weight;

    @Pattern(regexp = "([a-z=><]{1,7} )?[a-zA-Z0-9,._/-]{1,70}", message = "Invalid address") // may be more detail
    private String address;

    @Length(min = 1, max = 20, message = "Invalid identity length")
    @Pattern(regexp = "([a-z=><]{1,7} )?[a-zA-Z0-9]{1,20}", message = "Invalid identity") // may be more detail
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
        return Objects.isNull(this.page) ? null :
                Objects.isNull(this.size) ? Integer.MAX_VALUE : this.size;
    }
}
