package org.otis.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoPagingResponse extends DtoResponse {
    private int recordsTotal;
    private int recordsFiltered;
}
