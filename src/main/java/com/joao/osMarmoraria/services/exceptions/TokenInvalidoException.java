package com.joao.osMarmoraria.services.exceptions;

/**
 * Lançada quando um JWT apresentado não passa a verificação (assinatura
 * inválida, expirado, emissor incorreto, malformado...).
 *
 * <p>Substitui o anti-padrão de {@code TokenService.validateToken} devolver
 * uma string vazia em caso de falha: o valor mágico escondia o motivo real
 * da rejeição do log, e nada no tipo de retorno indicava ao chamador que
 * aquele {@code ""} significava "inválido" em vez de "login vazio". Ao
 * propagar a causa original de {@code JWTVerificationException}, o log do
 * {@code SecurityFilter} registra o motivo verdadeiro (expirado, assinatura,
 * emissor) sem alterar o comportamento HTTP observável: a requisição segue
 * sem autenticar e o entry point do Spring Security responde 401, como
 * antes.</p>
 */
public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
