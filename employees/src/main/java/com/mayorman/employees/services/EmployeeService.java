package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;

import java.util.List;

public interface EmployeeService  {

    EmployeeDto createEmployee(EmployeeDto employeeDetails);

    EmployeeDto updateEmployee(EmployeeDto employeeDetails);

    void deleteEmployee(String email);

    EmployeeDto  viewProfile(String email);

    //has to be a list

    public List<EmployeeDto> viewEmployeeDetails();

    EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization);
}
