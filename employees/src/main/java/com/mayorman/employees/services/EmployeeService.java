package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;

public interface EmployeeService  {

    EmployeeDto createEmployee(EmployeeDto employeeDetails);

    EmployeeDto updateEmployee(EmployeeDto employeeDetails);

    public void deleteEmployee();

    public void viewEmployeeDetails();

    //has to be a list

    public void viewProfile();

    EmployeeDto getEmployeeDetailsByEmail(String email);

    EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization);
}
