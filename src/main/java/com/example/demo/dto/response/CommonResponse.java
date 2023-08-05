package com.example.demo.dto.response;

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
     * @param success boolean: show main status of processing request
     * @param data List<T> | single Object T
     */
    public CommonResponse(boolean success, Object data) {
        if (data == null) throw new RuntimeException("data or size of the response is null");

        this.success = success ? SuccessState.TRUE : SuccessState.FALSE;
        if (success) this.data = data;
        else this.errors = data;
    }
}
