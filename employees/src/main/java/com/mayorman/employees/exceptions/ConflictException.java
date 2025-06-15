package com.mayorman.employees.exceptions;

public class ConflictException extends EmsException {

        public ConflictException(String message) {
            super(message);
        }
        public ConflictException(String message, Throwable cause) {
            super(message, cause);
        }
}

