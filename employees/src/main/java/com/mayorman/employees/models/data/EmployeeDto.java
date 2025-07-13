package com.mayorman.employees.models.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto implements Serializable {

    private  static final long serialVersionUID = -953297098295050686L;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String employeeId;
    private String encryptedPassword;
    private String department;
    private String gender;
    private String username;
    private String createdAt;
}
