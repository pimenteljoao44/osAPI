package com.joao.osMarmoraria.services.exceptions;

/**
 * Lançada ao tentar gerar contas a pagar para uma compra que já as possui.
 * Contraparte de {@link ContasReceberJaGeradasException} no fluxo de compras.
 */
public class ContasPagarJaGeradasException extends RegraDeNegocioException {

    private static final long serialVersionUID = 1L;

    public ContasPagarJaGeradasException(Integer compraId) {
        super("Contas a pagar já foram geradas para a compra " + compraId);
    }
}
