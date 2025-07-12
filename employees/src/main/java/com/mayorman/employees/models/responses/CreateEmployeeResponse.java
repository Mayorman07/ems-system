package com.mayorman.employees.models.responses;

import lombok.Data;

@Data
public class CreateEmployeeResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private String role;
    private String employeeId;
    private String username;

//    private Date lastLoggedIn;
    private String createdAt;
    private String updatedAt;
    private String department;


}
