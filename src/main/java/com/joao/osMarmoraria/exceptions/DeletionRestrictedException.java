package com.joao.osMarmoraria.exceptions;

import com.joao.osMarmoraria.services.exceptions.RegraDeNegocioException;

/**
 * Lançada ao tentar excluir um registro que possui vínculos que impedem a
 * exclusão (ex.: usuário com funcionário associado ou projetos criados).
 *
 * <p>Como toda regra de negócio, é respondida com HTTP 409 pelo handler único
 * — o {@code @ResponseStatus} que existia aqui conflitava com os dois
 * {@code @ControllerAdvice} antigos e tornava o status final imprevisível.</p>
 */
public class DeletionRestrictedException extends RegraDeNegocioException {

    private static final long serialVersionUID = 1L;

    public DeletionRestrictedException(String message) {
        super(message);
    }

    public DeletionRestrictedException(String message, Throwable cause) {
        super(message, cause);
    }
}
