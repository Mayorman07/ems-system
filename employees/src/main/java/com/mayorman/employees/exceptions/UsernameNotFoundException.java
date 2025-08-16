package com.mayorman.employees.exceptions;

public class UsernameNotFoundException extends EmsException{
    public UsernameNotFoundException(String message){super(message);}
    public UsernameNotFoundException(String message, Throwable cause) {super(message, cause);}

}