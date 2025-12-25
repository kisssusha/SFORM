package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ExistEntityException extends RuntimeException {
    public ExistEntityException(String message) {
        super(message);
    }
}
