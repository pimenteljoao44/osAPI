package com.joao.osMarmoraria.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Ou HttpStatus.CONFLICT (409) dependendo da preferência
public class DeletionRestrictedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DeletionRestrictedException(String message) {
        super(message);
    }

    public DeletionRestrictedException(String message, Throwable cause) {
        super(message, cause);
    }
}
