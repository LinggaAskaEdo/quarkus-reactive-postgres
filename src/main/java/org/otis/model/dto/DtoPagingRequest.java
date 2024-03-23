package org.otis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoPagingRequest {
    private int offset;
    private int limit;
    private DtoRequest search;
    private String order;
    private String sort;
}
