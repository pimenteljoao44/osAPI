package com.joao.osMarmoraria.services.exceptions;

/**
 * Lançada ao tentar gerar a ordem de serviço ou a conta a receber de uma
 * venda de projeto que já as possui. Fluxos idempotentes (reprocessar uma
 * venda) podem capturar este tipo especificamente e seguir normalmente; os
 * demais chamadores deixam o {@code ResourceExceptionHandler} responder 409.
 */
public class GeracaoDuplicadaException extends RegraDeNegocioException {

    private static final long serialVersionUID = 1L;

    public GeracaoDuplicadaException(String message) {
        super(message);
    }
}
