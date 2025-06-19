package com.mayorman.employees.models.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {
    @NotNull(message ="First name cannot be null")
    @Size(min = 2,message = "First name cant be less than two characters")
    @NotNull(message = "First name cannot be null")
    private String firstName;
    @NotNull(message ="Last name cannot be null")
    @Size(min = 2,message = "Last name cant be less than two characters")
    private String lastName;
    @Email
    private String email;
    @NotNull(message ="Password cannot be null")
    @Size(min = 3, max = 10, message = "Password must be between 3 and 10 characters !!")
    private String password;
    private String status;
    @NotNull(message = "Gender field cannot be null")
    @NotEmpty(message = "Gender field cannot be empty")
    private String gender;
    private String department;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
