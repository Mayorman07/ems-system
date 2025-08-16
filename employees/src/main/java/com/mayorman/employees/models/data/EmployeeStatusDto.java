package com.mayorman.employees.models.data;

import com.mayorman.employees.constants.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeStatusDto {

    private String firstName;
    private String lastName;
    private String email;
    private Status status;
    private String role;
    private String employeeId;
    private String username;
    //    private Date lastLoggedIn;
    private String createdAt;
    private String updatedAt;
    private String department;
}
