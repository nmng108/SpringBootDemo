package com.example.demo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class CommonResponse {
    private SuccessState success;
    private Object data;
    private Object errors;

    /**
     * @param success
     * @param data String
     */
    public CommonResponse(boolean success, Object data) {
        this.success = success ? SuccessState.TRUE : SuccessState.FALSE;
        if (success) this.data = data;
        else this.errors = data;
    }
}
