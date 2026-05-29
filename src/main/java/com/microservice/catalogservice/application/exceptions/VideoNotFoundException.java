package com.microservice.catalogservice.application.exceptions;

public class VideoNotFoundException extends BusinessException {
    public VideoNotFoundException(String message) {
        super(message);
    }
}
