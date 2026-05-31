package com.microservice.catalogservice.application.exceptions;

public class PostgreSqlException extends BusinessException {
    public PostgreSqlException(String message) {
        super(message);
    }
}
