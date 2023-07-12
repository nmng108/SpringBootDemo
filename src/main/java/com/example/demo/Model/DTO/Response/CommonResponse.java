package com.example.demo.Model.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public @Data class CommonResponse {
    private boolean success;
    private Object data;
    private Object errors;

    /**
     * @param success
     * @param data String
     */
    public CommonResponse(boolean success, Object data) {
        this.success = success;
        if (success) this.data = data;
        else this.errors = data;
    }
}
