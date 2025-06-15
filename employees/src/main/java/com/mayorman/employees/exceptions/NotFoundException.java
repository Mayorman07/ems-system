package com.mayorman.employees.exceptions;

public class NotFoundException extends EmsException{
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
