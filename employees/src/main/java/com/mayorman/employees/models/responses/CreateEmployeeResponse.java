package com.mayorman.employees.models.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private List<String> roles;
    private String employeeId;
    private String username;
//  private Date lastLoggedIn;
    private String createdAt;
    private String updatedAt;
    private String department;

    public CreateEmployeeResponse(String message) {
    }
}
