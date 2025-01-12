package com.mymoneymate.services.exceptions;

public class ValidationException extends ServiceException {
    public ValidationException(String message) {
        super(message);
    }
}