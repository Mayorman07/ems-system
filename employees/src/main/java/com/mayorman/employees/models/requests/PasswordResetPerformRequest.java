package com.mayorman.employees.models.requests;

import lombok.Data;

@Data
public class PasswordResetPerformRequest {
    private String token;
    private String newPassword;
}