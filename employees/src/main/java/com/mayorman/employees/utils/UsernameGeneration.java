package com.mayorman.employees.utils;

import org.springframework.stereotype.Component;

import java.util.Random;
@Component
public class UsernameGeneration {

    public String generateUsername(String firstName, String lastName) {
        String firstNamePart = firstName.length() < 3 ?
                firstName :
                firstName.substring(0, 3);
        String lastNamePart = lastName.length() < 2 ?
                lastName :
                lastName.substring(lastName.length() - 2);
        int randomNumber = new Random().nextInt(900) + 100;

        return (firstNamePart + lastNamePart).toLowerCase() + randomNumber;
    }
}
