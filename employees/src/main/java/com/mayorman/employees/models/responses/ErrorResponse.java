package com.mayorman.employees.models.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
public class ErrorResponse {

    private Date timestamp;
    private int status;
    private String message;

    public ErrorResponse(int status, String message) {
        this.timestamp = new Date();
        this.status = status;
        this.message = message;
    }
}
