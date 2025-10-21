package com.joao.osMarmoraria.exceptions.handler;

import com.joao.osMarmoraria.exceptions.DeletionRestrictedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeletionRestrictedException.class)
    public ResponseEntity<String> handleDeletionRestrictedException(DeletionRestrictedException ex) {
        // Retorna 400 Bad Request com a mensagem clara no corpo da resposta
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Você pode adicionar outros manipuladores de exceção aqui, se necessário
    // Ex: ResourceNotFoundException, DataIntegrityViolationException, etc.
}
