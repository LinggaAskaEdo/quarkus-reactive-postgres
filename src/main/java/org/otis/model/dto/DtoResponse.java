package org.otis.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoResponse {
    private String status;
    private String message;
    private String reason;
    private Object data;
}
