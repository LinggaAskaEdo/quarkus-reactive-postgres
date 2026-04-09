package org.otis.shared.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DtoPagingRequest {
    @Min(value = 0, message = "Offset must be >= 0")
    private int offset;

    @Min(value = 1, message = "Limit must be >= 1")
    private int limit;

    private DtoRequest search;
    private String order;
    private String sort;
}
