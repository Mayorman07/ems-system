package com.mayorman.employees.exceptions;

 public class EmsException extends RuntimeException{
     EmsException(String message){super (message); }
     EmsException(String message, Throwable cause){
            super(message, cause);
            if(this.getCause() == null && cause != null){
                this.initCause(cause);
            }
        }
    }

