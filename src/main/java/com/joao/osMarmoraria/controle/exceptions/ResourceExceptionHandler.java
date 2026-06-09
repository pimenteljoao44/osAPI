package com.joao.osMarmoraria.controle.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import com.joao.osMarmoraria.services.exceptions.RegraDeNegocioException;
import com.joao.osMarmoraria.services.exceptions.ValidationError;

/**
 * Handler único de exceções da API — todo erro flui por aqui e sai com o
 * mesmo envelope {@link StandardError}, num contrato previsível:
 *
 * <ul>
 *   <li><b>404</b> — recurso não encontrado ({@link ObjectNotFoundException})</li>
 *   <li><b>409</b> — conflito com regra de negócio ({@link RegraDeNegocioException} e filhas)</li>
 *   <li><b>400</b> — requisição inválida (integridade de dados e validação de campos)</li>
 * </ul>
 */
@ControllerAdvice
public class ResourceExceptionHandler {

	@ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<StandardError> recursoNaoEncontrado(ObjectNotFoundException e) {
		StandardError error = new StandardError(System.currentTimeMillis(),
				HttpStatus.NOT_FOUND.value(), e.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	public ResponseEntity<StandardError> regraDeNegocioViolada(RegraDeNegocioException e) {
		StandardError error = new StandardError(System.currentTimeMillis(),
				HttpStatus.CONFLICT.value(), e.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(DataIntegratyViolationException.class)
	public ResponseEntity<StandardError> violacaoDeIntegridade(DataIntegratyViolationException e) {
		StandardError error = new StandardError(System.currentTimeMillis(),
				HttpStatus.BAD_REQUEST.value(), e.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<StandardError> camposInvalidos(MethodArgumentNotValidException e) {
		ValidationError error = new ValidationError(System.currentTimeMillis(),
				HttpStatus.BAD_REQUEST.value(), "Erro na validação dos campos!");

		for (FieldError err : e.getBindingResult().getFieldErrors()) {
			error.addError(err.getField(), err.getDefaultMessage());
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}
