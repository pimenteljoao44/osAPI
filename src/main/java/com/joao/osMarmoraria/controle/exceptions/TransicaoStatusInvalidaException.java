package com.joao.osMarmoraria.controle.exceptions;

import com.joao.osMarmoraria.services.exceptions.RegraDeNegocioException;

/**
 * Lançada ao tentar mover uma entidade para um status que a máquina de
 * estados do domínio não permite a partir do status atual.
 *
 * <p>Como toda regra de negócio, é respondida com HTTP 409 pelo handler
 * único — a requisição é válida, mas conflita com o estado atual.</p>
 */
public class TransicaoStatusInvalidaException extends RegraDeNegocioException {
    public TransicaoStatusInvalidaException(String message) {
        super(message);
    }
}