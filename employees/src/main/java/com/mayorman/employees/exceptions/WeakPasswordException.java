package com.mayorman.employees.exceptions;

public class WeakPasswordException extends EmsException{
    public WeakPasswordException(String message) {
        super(message);
    }
    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}