package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface EmployeeService extends UserDetailsService {

    EmployeeDto createEmployee(EmployeeDto employeeDetails);

    EmployeeDto updateEmployee(EmployeeDto employeeDetails);

    void deleteEmployee(String email);

    EmployeeDto  viewProfile(String email);

    //has to be a list

   List<EmployeeDto> viewEmployeeDetails();

    EmployeeDto getEmployeeDetailsByEmail(String email);

    EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization);

}
