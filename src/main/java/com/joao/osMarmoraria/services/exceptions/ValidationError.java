package com.joao.osMarmoraria.services.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.joao.osMarmoraria.controle.exceptions.StandardError;

public class ValidationError extends StandardError {
	private static final long serialVersionUID = 1L;

	public ValidationError(Long timestmp, Integer status, String error) {
		super(timestmp, status, error);
	}
	
	
	private List<FieldMessage> errors = new ArrayList<>();

	public List<FieldMessage> getErrors() {
		return errors;
	}

	public void addError(String fieldName, String message) {
		this.errors.add(new FieldMessage( fieldName, message));
	}

	

}
