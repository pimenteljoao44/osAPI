package com.joao.osMarmoraria.controle.exceptions;

public class TransicaoStatusInvalidaException extends RuntimeException {
    public TransicaoStatusInvalidaException(String message) {
        super(message);
    }
}