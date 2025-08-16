package com.mayorman.employees.controllers;

import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.EmployeeStatusDto;
import com.mayorman.employees.models.requests.CreateEmployeeRequest;
import com.mayorman.employees.models.requests.EmployeeStatusCheckRequest;
import com.mayorman.employees.models.responses.CreateEmployeeResponse;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.responses.EmployeeStatusCheckResponse;
import com.mayorman.employees.services.EmployeeService;
import com.mayorman.employees.validations.InputValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@RefreshScope
public class EmployeesController {

    private final EmployeeService employeeService;
    private final ModelMapper modelMapper;
    @Autowired
    private Environment environment;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class); // Use SLF4J logger


    @GetMapping(path="/status/test")
    public String status() {
        logger.info("The incoming status check employee request");

        return "Working hard on my new api gateway route on the port of Dee" +" with token = " + environment.getProperty("token.secret.key") + "and the time is = "
                + environment.getProperty("token.expiration.time");
    }

    @PostMapping(path ="/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest employeeDetails, BindingResult bindingResult){
        logger.info("The incoming create employee request {} " , employeeDetails);
        InputValidator.validate(bindingResult);
        EmployeeDto employeeDto = modelMapper.map(employeeDetails, EmployeeDto.class);
        EmployeeDto createdUserDto = employeeService.createEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(createdUserDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }
    @PostMapping(path ="/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateEmployeeResponse> updateEmployee(@Valid @RequestBody CreateEmployeeRequest updateEmployeeDetails){
        logger.info("The incoming update employee request {} " , updateEmployeeDetails);
        EmployeeDto employeeDto = modelMapper.map(updateEmployeeDetails, EmployeeDto.class);
        EmployeeDto updatedEmployeeDto = employeeService.updateEmployee(employeeDto);

        CreateEmployeeResponse returnValue = modelMapper.map(updatedEmployeeDto,CreateEmployeeResponse.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }

    @GetMapping(path="/status/check")
    public ResponseEntity <EmployeeStatusCheckResponse> statusCheck(@Valid @RequestBody EmployeeStatusCheckRequest employeeStatusCheckRequest){
        logger.info("Received request to fetch status of employee with username: {}", employeeStatusCheckRequest);
        EmployeeStatusDto employeeStatusDto = modelMapper.map(employeeStatusCheckRequest,EmployeeStatusDto.class);
        EmployeeStatusDto requestedEmployeeStatusDto = employeeService.checkStatus(employeeStatusDto);
        logger.info("Received request to fetch status of employeezzz with from db username: {}", requestedEmployeeStatusDto);
        EmployeeStatusCheckResponse returnValue = modelMapper.map(requestedEmployeeStatusDto, EmployeeStatusCheckResponse.class);
        return ResponseEntity.status(HttpStatus.FOUND).body(returnValue);
    }

    @DeleteMapping(path ="/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("email") String email) {
        logger.info("Received request to delete employee with email: {}", email);
        employeeService.deleteEmployee(email); // Call the void service method
        logger.info("Employee with email {} deleted successfully.", email);
        return ResponseEntity.noContent().build();
    }
    @GetMapping(path ="/view/{email}")
    public ResponseEntity<EmployeeStatusCheckResponse> viewProfile(@PathVariable("email") String email){
        logger.info("Received request to view personal employee with email: {}", email);
        EmployeeStatusDto requestedEmployeeDetails = employeeService.viewProfile(email);
        logger.info("Employee profile with email {} viewed successfully.", email);
        EmployeeStatusCheckResponse returnValue = modelMapper.map(requestedEmployeeDetails,EmployeeStatusCheckResponse.class);
        return ResponseEntity.ok(returnValue);
    }

    @GetMapping(path ="/list")
    public ResponseEntity<List<CreateEmployeeResponse>> viewEmployeeDetails(){
        logger.info("Received request to fetch all employees available.");
        List<EmployeeDto> employeesDto = employeeService.viewEmployeeDetails();
        List<CreateEmployeeResponse> returnValue = employeesDto.stream()
                .map(employeeDto -> modelMapper.map(employeeDto, CreateEmployeeResponse.class))
                .collect(Collectors.toList());
        logger.info("Successfully fetched {} employees.", returnValue.size());
        return ResponseEntity.ok(returnValue);
    }

}
