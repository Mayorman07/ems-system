package com.mayorman.employees.models.data;

import com.mayorman.employees.constants.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

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
    private Status status;
    private List<String> roles;
}
