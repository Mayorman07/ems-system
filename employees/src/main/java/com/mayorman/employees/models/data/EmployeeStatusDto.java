package com.mayorman.employees.models.data;

import com.mayorman.employees.constants.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeStatusDto {

    private String firstName;
    private String lastName;
    private String email;
    private Status status;
//    private List<String> roles;
    private String roles; // change this to the above and check the mapping very well and the status check response
    private String employeeId;
    private String username;
    //    private Date lastLoggedIn;
    private String createdAt;
    private String updatedAt;
    private String department;
}
