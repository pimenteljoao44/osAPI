package com.joao.osMarmoraria.services.exceptions;

/**
 * Lançada ao tentar gerar contas a receber para uma venda que já as possui.
 * Fluxos idempotentes (ex.: reprocessar uma venda) capturam este tipo e seguem
 * normalmente; os demais deixam o handler responder 409.
 */
public class ContasReceberJaGeradasException extends RegraDeNegocioException {

    private static final long serialVersionUID = 1L;

    public ContasReceberJaGeradasException(Integer vendaId) {
        super("Contas a receber já foram geradas para a venda " + vendaId);
    }
}
