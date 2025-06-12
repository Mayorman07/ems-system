package com.mayorman.employees.controllers;

import com.mayorman.employees.models.CreateEmployeeRequest;
import com.mayorman.employees.models.CreateEmployeeResponses;
import com.mayorman.employees.models.CreateUserRequest;
import jakarta.validation.Valid;
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
public class EmployeesController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> createUser(@Valid @RequestBody CreateEmployeeRequest userDetails){

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = modelMapper.map(userDetails, UserDto.class);
        UserDto createdUserDto = usersService.createUser(userDto);

        CreateEmployeeResponse returnValue = modelMapper.map(createdUserDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
}
