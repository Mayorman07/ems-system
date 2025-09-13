package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.data.EmployeeStatusDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface EmployeeService extends UserDetailsService {
    EmployeeDto createEmployee(EmployeeDto employeeDetails);
    EmployeeDto updateEmployee(EmployeeDto employeeDetails);
    void deleteEmployee(String email);

    EmployeeStatusDto  viewProfile(String email);
    void updateLastLoggedIn(String employeeId);
   List<EmployeeDto> viewEmployeeDetails();
   List<EmployeeDto> getEmployeesInDepartment(String department);
    EmployeeDto getEmployeeDetailsByEmail(String email);
    void assignManagerRole(String employeeId);
    EmployeeStatusDto checkStatus(EmployeeStatusDto employeeStatusDto);
    EmployeeDto createInitialAdmin (EmployeeDto request);
    boolean requestPasswordReset(String email);
    boolean performPasswordReset(String token, String newPassword);
    boolean verifyUser(String token);
    int deactivateInactiveUsers();
}
