package com.mayorman.employees.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name="employees")
public class Employee implements Serializable {

    private static final long serialVersionUID = -273145678149216053L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 40)
    private String firstName;
    @Column(nullable = false, length = 40)
    private String lastName;
    @Column(nullable = false, length = 70,unique = true)
    private String email;
    @Column(nullable = false,unique = true)
    private String employeeId;
    @Column(nullable = false,unique = true)
    private String encryptedPassword;

}
