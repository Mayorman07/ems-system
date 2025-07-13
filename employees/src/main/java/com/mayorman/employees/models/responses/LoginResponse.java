package com.mayorman.employees.models.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {

    private String token;
    private String employeeId;
    private Long expirationMillis;

}
