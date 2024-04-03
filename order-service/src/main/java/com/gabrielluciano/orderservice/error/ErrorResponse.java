package com.gabrielluciano.orderservice.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String path;
    private String timestamp;
}
