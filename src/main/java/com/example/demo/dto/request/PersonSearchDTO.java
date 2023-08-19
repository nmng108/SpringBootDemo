package com.example.demo.dto.request;

import com.example.demo.validator.AcceptedStrings;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class PersonSearchDTO extends SearchDTO {
    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]+", message = "Invalid id") // may be more detail
    private String id;

    @Size(min = 5, max = 45, message = "Invalid name")
    @Pattern(regexp = "([A-Z][a-z]{2,20})( [A-Z][a-z]{2,20}){1,3}", message = "Invalid name")
    private String name;

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]{4}-[0-9]{2}-[0-9]{2}", message = "Invalid birthDate")
    // may be more detail
    private String birthDate;

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9,.]{1,6}", message = "Invalid height") // may be more detail
    private String height; // unit: cm

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9,.]{1,6}", message = "Invalid weight") // may be more detail
    private String weight;

    @Pattern(regexp = "([a-z=><]{1,7} )?[a-zA-Z0-9,._/-]{1,70}", message = "Invalid address") // may be more detail
    private String address;

    @Size(min = 1, max = 20, message = "Invalid identity length")
    @Pattern(regexp = "([a-z=><]{1,7} )?[a-zA-Z0-9]{1,20}", message = "Invalid identity") // may be more detail
    private String identity;
}
