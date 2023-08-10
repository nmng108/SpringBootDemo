package com.example.demo.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties("storage")
@Getter
@Setter
public class StorageProperties {
    private final String location = "uploads";

    private final String vehicleImageLocation = this.location.concat("/vehicle-images");
    private final int maxImagesPerVehicle = 3;

    @DataSizeUnit(DataUnit.MEGABYTES)
    private final DataSize maxImageSizeInMB = DataSize.ofMegabytes(5);
    private final Set<String> allowedImageTypes = new HashSet<>();

    public StorageProperties() {
        this.allowedImageTypes.addAll(Arrays.asList("png", "jpg", "jpeg"));
    }
}
