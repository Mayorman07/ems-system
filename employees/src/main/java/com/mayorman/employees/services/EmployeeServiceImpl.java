package com.mayorman.employees.services;

import com.mayorman.employees.constants.Status;
import com.mayorman.employees.entities.Authority;
import com.mayorman.employees.entities.Employee;
import com.mayorman.employees.entities.Role;
import com.mayorman.employees.exceptions.ConflictException;
import com.mayorman.employees.exceptions.NotFoundException;
import com.mayorman.employees.models.data.CustomUserDetails;
import com.mayorman.employees.models.data.EmployeeDto;
import com.mayorman.employees.models.data.EmployeeStatusDto;
import com.mayorman.employees.models.data.PasswordResetEventDto;
import com.mayorman.employees.models.data.UserCreatedEventDto;
import com.mayorman.employees.repository.EmployeeRepository;
import com.mayorman.employees.repository.RoleRepository;
import java.util.Calendar;

import com.mayorman.employees.utils.UsernameGeneration;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final Environment environment;
    public UsernameGeneration usernameGeneration;
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
//        String username = generateUsername(employeeDetails.getFirstName(), employeeDetails.getLastName());
//        employeeDetails.setUsername(username);
        employeeDetails.setUsername(usernameGeneration.generateUsername(employeeDetails.getFirstName(),employeeDetails.getLastName()));
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
    @Override
    public EmployeeStatusDto viewProfile(String email) {
        Employee existingEmployee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.info("Employee with email {} not found for viewing!", email);
                    return new NotFoundException("Employee not found!");
                });

        return modelMapper.map(existingEmployee, EmployeeStatusDto.class);
    }
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
    public List<EmployeeDto> getEmployeesInDepartment(String department) {
        List<Employee> employeesInDepartment = employeeRepository.findAllByDepartment(department);
        if (employeesInDepartment.isEmpty()) {
            logger.info("No employees found for department: {}", department);
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<EmployeeDto>>() {}.getType();
        List<EmployeeDto> returnValue = new ModelMapper().map(employeesInDepartment, listType);
        return returnValue;
    }

    @Override
    public EmployeeDto getEmployeeDetailsByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    @Transactional
    public void assignManagerRole(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));
        Role managerRole = roleRepository.findByName("ROLE_MANAGER");
        if (managerRole == null) {
            throw new RuntimeException("Error: ROLE_MANAGER not found.");
        }
        Collection<Role> userRoles = employee.getRoles();
        if (!userRoles.contains(managerRole)) {
            userRoles.add(managerRole);
            employee.setRoles(userRoles);

            employeeRepository.save(employee);
        }
    }

    @Override
    public EmployeeStatusDto checkStatus(EmployeeStatusDto employeeStatusDto) {
        Employee foundEmployee = employeeRepository.findEmployeeByUsername(employeeStatusDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(employeeStatusDto.getUsername()));

        logger.info("Received request to fetch status of employee with username: {}", foundEmployee.getUsername());
        return modelMapper.map(foundEmployee, EmployeeStatusDto.class);
    }

    public EmployeeDto createInitialAdmin(EmployeeDto request) {
        if (employeeRepository.count() > 0) {
            throw new IllegalStateException("An initial admin user already exists. This endpoint cannot be used again.");
        }
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("Error: ROLE_ADMIN not found in the database.");
        }
        Employee adminEntity = modelMapper.map(request, Employee.class);

        // 4. Set the fields that require special logic
        adminEntity.setEmployeeId(UUID.randomUUID().toString());
        adminEntity.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
        adminEntity.setVerificationToken(UUID.randomUUID().toString());
        adminEntity.setStatus(Status.ACTIVE);
        adminEntity.setRoles(Arrays.asList(adminRole));
        adminEntity.setUsername(usernameGeneration.generateUsername(request.getFirstName(), request.getLastName()));
        Employee savedAdmin = employeeRepository.save(adminEntity);
        EmployeeDto returnDto = modelMapper.map(savedAdmin, EmployeeDto.class);
        List<String> roleNames = savedAdmin.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        returnDto.setRoles(roleNames);
        return returnDto;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Employee> employeeToBeLoggedIn = employeeRepository.findByEmail(username);
        if(employeeToBeLoggedIn.isEmpty()){
            throw new UsernameNotFoundException(username);
        }

        if (employeeToBeLoggedIn.get().getStatus() != Status.ACTIVE) {
            throw new DisabledException("User account is not active. Status: " + employeeToBeLoggedIn.get().getStatus());
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Collection<Role> roles = employeeToBeLoggedIn.get().getRoles();

        roles.forEach((role) ->{
            authorities.add(new SimpleGrantedAuthority((role.getName())));
            Collection<Authority> authorityEntities = role.getAuthorities();

            authorityEntities.forEach((authorityEntity -> {
                authorities.add(new SimpleGrantedAuthority(authorityEntity.getName()));
            }));
        });

        //enabled after password can be false until the user successfully verifys their email
        return new CustomUserDetails(employeeToBeLoggedIn.get().getEmail(), employeeToBeLoggedIn.get().getEncryptedPassword(),
                true, true, true,true,
                authorities,employeeToBeLoggedIn.get().getEmployeeId(),
                employeeToBeLoggedIn.get().getDepartment());
    }
    @Transactional
    public void updateLastLoggedIn(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with ID: " + employeeId));
        employee.setLastLoggedIn(new Date());
        employeeRepository.save(employee);
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
        UserCreatedEventDto eventDto = new UserCreatedEventDto(
                savedEmployee.getFirstName(),
                savedEmployee.getEmail(),
                verificationToken
        );
        rabbitTemplate.convertAndSend("user-events-exchange", "user.created", eventDto);
        logger.info("Published User Created event for email: {}", savedEmployee.getEmail());
    }

    @Transactional
    public boolean requestPasswordReset(String email) {
        Optional<Employee> employeeOptional = employeeRepository.findByEmail(email);
        logger.info("See the employee returned {} ", employeeOptional);

        if (employeeOptional.isEmpty()) {
            // We return true but do nothing to prevent email enumeration attacks.
            logger.warn("Password reset requested for non-existent email: {}", email);
            return true;
        }

        Employee employee = employeeOptional.get();
        String token = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);

        employee.setPasswordResetToken(token);
        employee.setPasswordResetTokenExpiryDate(cal.getTime());
        employeeRepository.save(employee);

        // Publish the event to RabbitMQ
        PasswordResetEventDto eventDto = new PasswordResetEventDto(
                employee.getEmail(),
                employee.getFirstName(),
                token
        );
        rabbitTemplate.convertAndSend("password-reset-email-queue", eventDto);

        logger.info("Published password reset event for email: {}", email);
        return true;
    }

    @Transactional
    public boolean performPasswordReset(String token, String newPassword) {
        // 1. Find the user by their password reset token
        Optional<Employee> employeeOptional = employeeRepository.findByPasswordResetToken(token);

        if (employeeOptional.isEmpty()) {
            logger.warn("Password reset attempted with an invalid token.");
            return false; // Token was not found
        }
        Employee employee = employeeOptional.get();
        // 2. Check if the token has expired
        if (employee.getPasswordResetTokenExpiryDate().before(new Date())) {
            logger.warn("Expired password reset token used for user: {}", employee.getEmail());
            // Invalidate the expired token for security
            employee.setPasswordResetToken(null);
            employee.setPasswordResetTokenExpiryDate(null);
            employeeRepository.save(employee);
            return false; // Token has expired
        }
        // 3. If the token is valid, update the password
        employee.setEncryptedPassword(passwordEncoder.encode(newPassword));

        // 4. CRITICAL: Invalidate the token so it cannot be used again
        employee.setPasswordResetToken(null);
        employee.setPasswordResetTokenExpiryDate(null);

        employeeRepository.save(employee);

        logger.info("Password successfully reset for user: {}", employee.getEmail());
        return true;
    }

    @Transactional
    public int deactivateInactiveUsers() {
        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MINUTE, -3);
        cal.add(Calendar.MONTH, -2);
        Date cutoffDate = cal.getTime();
        List<Employee> potentiallyInactiveUsers = employeeRepository.findAllByStatusAndLastLoggedInBefore(Status.ACTIVE, cutoffDate);
        // 3. Filter out any admins from that list
        List<Employee> finalUsersToDeactivate = potentiallyInactiveUsers.stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());
        if (finalUsersToDeactivate.isEmpty()) {
            return 0;
        }
        for (Employee user : finalUsersToDeactivate) {
            user.setStatus(Status.DEACTIVATED);
        }
        employeeRepository.saveAll(finalUsersToDeactivate);

        logger.info("SUCCESS: Deactivated {} users", finalUsersToDeactivate.size());
        return finalUsersToDeactivate.size();
    }
}