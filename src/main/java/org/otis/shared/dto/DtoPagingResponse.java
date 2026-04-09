package org.otis.shared.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DtoPagingResponse extends DtoResponse {
    private int recordsTotal;
    private int recordsFiltered;
}
