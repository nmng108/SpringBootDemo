package com.example.demo.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public final class VehicleImageUploadDTO {
    List<MultipartFile> images;
}
