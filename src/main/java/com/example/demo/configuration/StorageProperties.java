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
    private String location = "uploads";
    private String vehicleImageLocation = this.location.concat("/vehicle-images");

    @DataSizeUnit(DataUnit.MEGABYTES)
    private DataSize maxImageSizeInMB = DataSize.ofMegabytes(5);
    private Set<String> allowedImageTypes = new HashSet<>();

    public StorageProperties() {
        this.allowedImageTypes.addAll(Arrays.asList("png", "jpg", "jpeg"));
    }
}
