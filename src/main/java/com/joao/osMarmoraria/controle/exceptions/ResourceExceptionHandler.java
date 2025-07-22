package com.joao.osMarmoraria.controle.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import com.joao.osMarmoraria.services.exceptions.ValidationError;

@ControllerAdvice
public class ResourceExceptionHandler {

	@ExceptionHandler(ObjectNotFoundException.class)
	private ResponseEntity<StandardError> objectNotFound(ObjectNotFoundException e){
		StandardError error = new StandardError(System.currentTimeMillis(),
				HttpStatus.NOT_FOUND.value(),e.getMessage());
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(DataIntegratyViolationException.class)
	private ResponseEntity<StandardError> objectNotFound(DataIntegratyViolationException e){
		StandardError error = new StandardError(System.currentTimeMillis(),
				HttpStatus.BAD_REQUEST.value(),e.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	private ResponseEntity<StandardError> objectNotFound(MethodArgumentNotValidException e){
		ValidationError error = new ValidationError(System.currentTimeMillis(),
				HttpStatus.BAD_REQUEST.value(),"Erro na validação dos campos!");
		
		for(FieldError err : e.getBindingResult().getFieldErrors()) {
			error.addError(err.getField(), err.getDefaultMessage());
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}
