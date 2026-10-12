package com.joao.osMarmoraria.services.exceptions;

/**
 * Base das exceções de regra de negócio: a requisição é bem formada, mas
 * conflita com o estado atual do sistema (ex.: gerar contas a receber duas
 * vezes, excluir um registro com vínculos, transição de status inválida).
 *
 * <p>Mapeada para HTTP 409 (Conflict) pelo {@code ResourceExceptionHandler}.
 * Lance uma subclasse específica quando o chamador precisar reagir ao caso
 * (capturando o <em>tipo</em>, nunca comparando a mensagem via
 * {@code getMessage().contains(...)} — controle de fluxo por texto quebra
 * silenciosamente se a mensagem mudar, sem nenhum aviso do compilador).</p>
 */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String message) {
        super(message);
    }

    public RegraDeNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
