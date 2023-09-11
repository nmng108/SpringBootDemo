package com.example.demo.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VehicleSearchDTO extends SearchDTO {
    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]+", message = "Invalid id") // may be more detail
    private String id;

    @Size(min = 5, max = 45, message = "Invalid idNumber")
//    @Pattern(regexp = "([A-Z][a-z]{2,20})( [A-Z][a-z]{2,20}){1,3}", message = "Invalid name")
    private String idNumber;

    @Size(min = 1, max = 20, message = "Invalid identity length")
    @Pattern(regexp = "([a-z=><]{1,7} )?[a-zA-Z0-9]{1,20}", message = "Invalid identity") // may be more detail
    private String ownerIdentity;

//    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9,.]{1,6}", message = "Invalid height") // may be more detail
    private String type; // unit: cm

    private String brand; // unit: cm

    private String model; // unit: cm

    @Pattern(regexp = "([a-z=><]{1,7} )?[0-9]{4}-[0-9]{2}-[0-9]{2}", message = "Invalid acquisitionDate")
    // may be more detail
    private String acquisitionDate;
}
