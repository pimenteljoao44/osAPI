package com.joao.osMarmoraria.services.exceptions;

/**
 * Lançada quando um token JWT não passa na verificação (assinatura inválida,
 * expirado, emissor errado). Carrega a causa original para diagnóstico —
 * substitui o antigo retorno de string vazia, que engolia o motivo da falha.
 */
public class TokenInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
