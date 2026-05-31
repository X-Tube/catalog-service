package com.microservice.catalogservice.application.exceptions;

public class CloudFrontException extends BusinessException {
    public CloudFrontException(String messages) {
        super(messages);
    }
}
