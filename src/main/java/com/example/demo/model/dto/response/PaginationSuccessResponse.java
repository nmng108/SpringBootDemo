package com.example.demo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class PaginationSuccessResponse<T> {
    private SuccessState success;
    private Long totalPages;
    private List<T> data;

    /**
     * @param success boolean: show main status of processing request
     * @param data    List<T>
     */
    public PaginationSuccessResponse(boolean success, List<T> data, Long totalRecords, Long size) {
        if (data == null || size == null) throw new RuntimeException("data or size of the response is null");

        this.success = success ? SuccessState.TRUE : SuccessState.FALSE;
        this.data = data;
        double doublePages = (double) totalRecords / size;
        this.totalPages = doublePages > (long) doublePages ? (long) doublePages + 1 : (long) doublePages;
    }

    public PaginationSuccessResponse(boolean success, Long totalRecords, Long size) {
        if (size == null) throw new RuntimeException("data or size of the response is null");

        this.success = success ? SuccessState.TRUE : SuccessState.FALSE;
        double doublePages = (double) totalRecords / size;
        this.totalPages = doublePages > (long) doublePages ? (long) doublePages + 1 : (long) doublePages;
    }
}
