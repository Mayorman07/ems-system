package com.mayorman.employees.services;

import com.mayorman.employees.constants.Status;
import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.exceptions.ConflictException;
import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.data.EmployeeStatusDto;
import com.mayorman.employees.models.data.UserCreatedEventDto;
import com.mayorman.employees.models.requests.EmployeeStatusCheckRequest;
import com.mayorman.employees.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    // --- Dependencies are correctly defined here ---
    private final EmployeeRepository employeeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder; // <-- IMPROVEMENT 1: Using the interface
    private final ModelMapper modelMapper;
    private final Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Override
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDetails) {
        if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
            logger.info("Employee with email {} already exists!", employeeDetails.getEmail());
            throw new ConflictException("Existing employee!");
        }

        employeeDetails.setEmployeeId(UUID.randomUUID().toString());
        employeeDetails.setEncryptedPassword(passwordEncoder.encode(employeeDetails.getPassword()));
        String username = generateUsername(employeeDetails.getFirstName(), employeeDetails.getLastName());
        employeeDetails.setUsername(username);
        employeeDetails.setStatus(Status.INACTIVE);
        Employee employeeToBeCreated = modelMapper.map(employeeDetails, Employee.class);
        String verificationToken = UUID.randomUUID().toString();
        employeeToBeCreated.setVerificationToken(verificationToken);
        Employee savedEmployee = employeeRepository.save(employeeToBeCreated);
        publishUserCreatedEvent(savedEmployee, verificationToken);
        return modelMapper.map(savedEmployee, EmployeeDto.class);
    }


    @Override
    @Transactional
    public EmployeeDto updateEmployee(EmployeeDto employeeDetails) {
        Employee existingEmployee = employeeRepository.findByEmail(employeeDetails.getEmail())
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for update!", employeeDetails.getEmail());
                    return new NotFoundException("Employee not found!");
                });

        modelMapper.map(employeeDetails, existingEmployee);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return modelMapper.map(updatedEmployee, EmployeeDto.class);
    }

    @Override
    @Transactional
    public void deleteEmployee(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for deletion!", email);
                    return new NotFoundException("Employee not found!");
                });

        employeeRepository.delete(existingEmployee);
        logger.info("The employee has been deleted");
    }

    // this is for a singular employee for themselves
    @Override
    public EmployeeStatusDto viewProfile(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for viewing!", email);
                    return new NotFoundException("Employee not found!");
                });

        return modelMapper.map(existingEmployee, EmployeeStatusDto.class);
    }

    // this is for the admin, add a check here to verify only the admin can do this
    @Override
    public List<EmployeeDto> viewEmployeeDetails() {
        List<Employee> existingEmployees = employeeRepository.findAll();
        if (existingEmployees.isEmpty()) {
            logger.info("No employees found in the database.");
            return List.of();
        }
        return existingEmployees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .collect(Collectors.toList());
    }
    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return modelMapper.map(employee, EmployeeDto.class);
    }
    @Override
    public EmployeeStatusDto checkStatus(EmployeeStatusDto employeeStatusDto) {
        Employee foundEmployee = employeeRepository.findEmployeeByUsername(employeeStatusDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(employeeStatusDto.getUsername()));

        logger.info("Received request to fetch status of employee with username: {}", foundEmployee.getUsername());
        return modelMapper.map(foundEmployee, EmployeeStatusDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Employee> employeeToBeLoggedIn = employeeRepository.findByEmail(username);
        if(employeeToBeLoggedIn.isEmpty()){
            throw new UsernameNotFoundException(username);
        }
        return new User(employeeToBeLoggedIn.get().getEmail(), employeeToBeLoggedIn.get().getEncryptedPassword(),true,
                true, true,true,new ArrayList<>());
    }

    private String generateUsername(String firstName, String lastName) {
        String firstNamePart = firstName.length() < 3 ?
                firstName :
                firstName.substring(0, 3);
        String lastNamePart = lastName.length() < 2 ?
                lastName :
                lastName.substring(lastName.length() - 2);
        int randomNumber = new Random().nextInt(900) + 100;

        return (firstNamePart + lastNamePart).toLowerCase() + randomNumber;
    }
    public boolean verifyUser(String token) {
        Optional<Employee> employeeWithVerificationToken = employeeRepository.findByVerificationToken(token);
        if (employeeWithVerificationToken.isPresent()) {
            Employee employee = employeeWithVerificationToken.get();
            employee.setStatus(Status.ACTIVE);
            employee.setVerificationToken(null); // Clear the token so it can't be used again
            employeeRepository.save(employee);
            return true;
        }
        return false;
    }

    private void publishUserCreatedEvent(Employee savedEmployee, String verificationToken) {
        // Create the specific DTO for the event
        UserCreatedEventDto eventDto = new UserCreatedEventDto(
                savedEmployee.getFirstName(),
                savedEmployee.getEmail(),
                verificationToken
        );
        // Send the correct eventDto to RabbitMQ
        rabbitTemplate.convertAndSend("user-events-exchange", "user.created", eventDto);
        logger.info("Published User Created event for email: {}", savedEmployee.getEmail());
    }
}