package com.mayorman.employees.controllers;

import com.mayorman.employees.models.requests.CreateEmployeeRequest;
import com.mayorman.employees.models.responses.CreateEmployeeResponse;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeesController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> createUser(@Valid @RequestBody CreateEmployeeRequest employeeDetails){

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        EmployeeDto userDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto createdUserDto = employeeService.createEmployee(userDto);

        CreateEmployeeResponse returnValue = modelMapper.map(createdUserDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
}
