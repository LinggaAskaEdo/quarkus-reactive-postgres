package org.otis.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoPagingRequest {
    @Min(value = 0, message = "Offset must be greater than or equal to 0")
    private int offset;

    @Min(value = 1, message = "Limit must be greater than 0")
    private int limit;

    private DtoRequest search;

    private String order;

    @Pattern(regexp = "^(?i)(asc|desc)?$", message = "Sort must be ASC or DESC")
    private String sort;
}
