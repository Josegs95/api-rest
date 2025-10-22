package com.example.api_rest.exception;

public class VideoGameNotFoundException extends NotFoundException {
    public VideoGameNotFoundException(String message) {
        super(message);
    }
}
