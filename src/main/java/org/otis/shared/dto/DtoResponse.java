package org.otis.shared.dto;

import lombok.Data;

@Data
public class DtoResponse {
    private String status;
    private String message;
    private String reason;
    private Object data;
}
