package com.mayorman.employees.exceptions;

public class BadRequestException extends EmsException{
    public BadRequestException(String message){super(message);}
    public BadRequestException(String message, Throwable cause) {super(message, cause);}

}
