package com.example.api_rest.exception;

public class UserNotFoundException extends NotFoundException{

    public UserNotFoundException(String message) {
        super(message);
    }
}
