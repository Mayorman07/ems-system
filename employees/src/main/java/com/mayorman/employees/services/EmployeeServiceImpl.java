package com.mayorman.employees.services;

import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeServiceImpl implements  EmployeeService{

    EmployeeRepository employeeRepository;

    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDetails) {
        return null;
    }

    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        return null;
    }

    @Override
    public EmployeeDto getEmployeeByEmployeeId(String employeeId, String authorization) {
        return null;
    }
}
