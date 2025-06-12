package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;

public interface EmployeeService  {

    EmployeeDto createEmployee(EmployeeDto employeeDetails);
    EmployeeDto getEmployeeDetailsByEmail(String email);

    EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization);
}
