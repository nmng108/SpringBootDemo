package com.example.demo.Controller;

import com.example.demo.Model.DTO.Request.VehicleCreationDTO;
import com.example.demo.Model.DTO.Request.VehicleDeletionDTO;
import com.example.demo.Model.DTO.Response.CommonResponse;
import com.example.demo.Service.VehicleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    @Getter
    private VehicleService service;

    @GetMapping
    public ResponseEntity<CommonResponse> fetchAll() {
        return this.getService().findAll();
    }

    @PostMapping
    public ResponseEntity<CommonResponse> create(
            @RequestBody @Valid VehicleCreationDTO dto, BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) return errorResponse(bindingResult);

            return this.getService().save(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping
    public ResponseEntity<CommonResponse> delete(@RequestBody VehicleDeletionDTO dto) {
        return this.getService().delete(dto.getId());
    }

    private ResponseEntity<CommonResponse> errorResponse(BindingResult bindingResult) {
        HashMap<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach((error) -> {
            errors.put(error.getField(), error.getDefaultMessage());
            System.out.println(error);
        });

        HashMap<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error_code", "E00");
        errorMessage.put("details", errors);

        return ResponseEntity.badRequest().body(
                CommonResponse.builder().success(false).errors(errorMessage).build()
        );
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<CommonResponse> handleException(Exception e) {
        e.printStackTrace();

        return ResponseEntity.internalServerError().body(
                CommonResponse.builder().success(false).errors(e.getMessage()).build()
        );
    }
}
