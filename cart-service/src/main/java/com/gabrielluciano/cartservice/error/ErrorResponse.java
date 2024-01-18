package com.gabrielluciano.cartservice.error;

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
