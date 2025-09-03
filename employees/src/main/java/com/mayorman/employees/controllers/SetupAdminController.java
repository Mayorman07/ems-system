package com.mayorman.employees.controllers;

import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.requests.CreateAdminRequest;
import com.mayorman.employees.models.requests.CreateEmployeeRequest;
import com.mayorman.employees.models.responses.CreateEmployeeResponse;
import com.mayorman.employees.services.EmployeeService;
import com.mayorman.employees.validations.InputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupAdminController {

    private final EmployeeService employeeService;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class); // Use SLF4J logger

    @PostMapping("/create-admin")
    public ResponseEntity<CreateEmployeeResponse> createInitialAdmin( @Valid @RequestBody CreateAdminRequest request, BindingResult bindingResult){
        logger.info("The incoming create employee request {} " , request);
        InputValidator.validate(bindingResult);
        try {
            EmployeeDto adminDto = modelMapper.map(request, EmployeeDto.class);
            EmployeeDto createdAdminDto = employeeService.createInitialAdmin(adminDto);
            CreateEmployeeResponse returnValue= modelMapper.map(createdAdminDto,CreateEmployeeResponse.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
        }
         catch (IllegalStateException e) {
            // This error is thrown if the endpoint is called a second time
             CreateEmployeeResponse errorResponse = new CreateEmployeeResponse(e.getMessage());
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }catch (NotFoundException e) {
            // This handles other errors, like the ROLE_ADMIN not being found
            CreateEmployeeResponse errorResponse = new CreateEmployeeResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
