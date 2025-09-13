package com.mayorman.employees.controllers;

import com.mayorman.employees.models.requests.PasswordResetPerformRequest;
import com.mayorman.employees.models.requests.PasswordResetRequest;
import com.mayorman.employees.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final EmployeeService employeeService;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        employeeService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("If an account with that email exists, a password reset token has been sent.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> performPasswordReset(@Valid @RequestBody PasswordResetPerformRequest request) {

        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("Password reset token cannot be null or empty.");
        }
        logger.info("Attempting to reset password with token: {}", request.getToken());
        boolean isSuccessful = employeeService.performPasswordReset(request.getToken(), request.getNewPassword());
        logger.info("Attempting to reset password with token Twooo: {}", request.getToken());

        if (isSuccessful) {
            return ResponseEntity.ok("Your password has been successfully updated.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The password reset link is invalid or has expired. Please request a new one.");
        }
    }
}
