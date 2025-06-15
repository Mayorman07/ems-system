package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;

public interface EmployeeService  {

    EmployeeDto createEmployee(EmployeeDto employeeDetails);

    EmployeeDto updateEmployee(EmployeeDto employeeDetails);

    void deleteEmployee(String email);

    EmployeeDto  viewEmployeeDetails(String email);

    //has to be a list

    public void viewProfile();

    EmployeeDto getEmployeeDetailsByEmail(String email);

    EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization);
}
